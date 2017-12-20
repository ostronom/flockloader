(ns flockloader.fetch-worker
  (:require [clojure.core.async :as async :refer [<! >! put! chan go-loop]]
            [taoensso.timbre :as log]
            [org.httpkit.client :as http]
            [flockloader.parser :as parser]))

(defn encode [term]
  (java.net.URLEncoder/encode term "UTF-8"))

(defn status-error [status]
  (RuntimeException. (str "Request returned" status "code")))

(defn process-result [term {:keys [status body error]}]
  (cond
    (some? error)         {:error error :term term}
    (not (== status 200)) {:error (status-error status) :term term}
    :else                 {:ok (parser/extract-links body)}))

(defn spawn [worker-id client options tasks-chan]
  (let [worker-chan (chan)]
    (go-loop []
      (log/debug "Worker #" worker-id "ready to accept tasks")
      (when-let [{:keys [term resp-chan]} (<! tasks-chan)]
        (log/debug "Worker #" worker-id "got term" (str "`" term "`"))
        (http/get
          (str "https://www.bing.com/search?q=" (encode term) "&format=rss&count=" count)
          options
          #(put! worker-chan %))
        (>! resp-chan (process-result term (<! worker-chan)))
        (recur)))))
