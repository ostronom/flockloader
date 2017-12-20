(ns flockloader.fetch
  (:import [org.httpkit.client HttpClient])
  (:require [flockloader.fetch-worker :as worker]
            [org.httpkit.client :as http]
            [clojure.set :refer [union]]
            [clojure.core.async :as async :refer [chan <! >! put! go go-loop]]
            [taoensso.timbre :as log]))

(defprotocol BingFetching
  (fetch-terms [_ terms]))

(defn result->results [results {:keys [ok error term] :as r}]
  (cond
    (some? ok)    (union results ok)
    (some? error) (do
                    (log/error "Error" error (str "while working on term `" term "`"))
                    results)
    :else         (do
                    (log/error "Unexpected result" r)
                    results)))

(defrecord Fetcher [tasks-chan]
  BingFetching
  (fetch-terms [_ terms]
    (let [terms-num (count terms)
          resp-chan (chan terms-num)
          output    (chan)]
      (go (>! tasks-chan {:terms terms :resp-chan resp-chan}))
      ;; loop until all terms are resolved either to ok-result or to an error
      (go-loop [left    terms-num
                results #{}]
        (if (zero? left)
          (do
            (async/close! resp-chan)
            (>! output results))
          (if-let [result (<! resp-chan)]
            (recur (dec left) (result->results results result))
            (do
              (log/error "Fetcher channel closed unexpectedly")
              (recur 0 results)))))
      output)))

(defn start-fetcher [num-workers timeout]
  ;; Starts `globally bounded` fetcher which limits ALL connections to num-workers
  ;; count. There is a possibility to use `locally bounded` fetcher which is
  ;; essentially a `globally bounded` one for each task (set of terms)
  (let [options          {:timeout timeout :as :stream}
        client           (http/HttpClient. num-workers)
        outer-tasks-chan (chan)
        inner-tasks-chan (chan num-workers)]
    (doseq [worker-id (range 0 num-workers)]
      (worker/spawn worker-id client options inner-tasks-chan))
    (go-loop []
      (when-let [{:keys [terms resp-chan]} (<! outer-tasks-chan)]
        (doseq [term terms]
          (>! inner-tasks-chan {:term term :resp-chan resp-chan}))
        (recur)))
    (->Fetcher outer-tasks-chan)))
