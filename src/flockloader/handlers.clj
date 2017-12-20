(ns flockloader.handlers
  (:require [org.httpkit.server :refer [with-channel send!]]
            [clojure.core.async :as async :refer [<! go]]
            [jsonista.core :as json]
            [taoensso.timbre :as log]
            [flockloader.combine :as combine]))

(def mapper
  (json/object-mapper {:pretty true}))

(defn prettify [s] (json/write-value-as-string s mapper))

(defn search [fetcher {:keys [params] :as req}]
  (let [query (:query params)]
    (if (empty? query)
      {:status 400 :body "No terms provided"}
      (with-channel req ch
        (go
          (let [resp (<! (combine/get-combined-result fetcher (distinct query)))]
            (send! ch {:status 200
                       :headers {"Content-Type" "application/json"}
                       :body (prettify resp)})))))))
