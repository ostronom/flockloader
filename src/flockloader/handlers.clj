(ns flockloader.handlers
  (:require [org.httpkit.server :refer [with-channel send!]]
            [clojure.core.async :as async :refer [<! alts! timeout go]]
            [jsonista.core :as json]
            [taoensso.timbre :as log]
            [flockloader.combine :as combine]))

(def mapper
  (json/object-mapper {:pretty true}))

(defn prettify [s] (json/write-value-as-string s mapper))

(defn to-coll [x]
  (if (string? x) [x] x))

(defn search [fetcher timeout-in-ms {:keys [params] :as req}]
  (let [query (:query params)]
    (if (empty? query)
      {:status 400 :body "No terms provided"}
      (with-channel req ch
        (go
          (let [timer  (timeout timeout-in-ms)
                result (combine/get-combined-result fetcher (distinct (to-coll query)))
                [v p]  (alts! [timer result])]
            (send! ch
              (if (nil? v)
                {:status 408 :body "Request timed out"}
                {:status 200
                  :headers {"Content-Type" "application/json"}
                  :body (prettify v)}))))))))
