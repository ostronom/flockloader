(ns flockloader.core
  (:require [flockloader.fetch :as fetch]
            [flockloader.handlers :as h]
            [taoensso.timbre :as log]
            [org.httpkit.server :as http]
            [compojure.handler :refer [site]]
            [compojure.core :refer [defroutes GET]]))


(def fetcher (fetch/start-fetcher 10 1500))

(defroutes all-routes
  (GET "/search" req (h/search fetcher req)))

(defn wrap-request-logging [handler]
  (fn [{:keys [request-method uri] :as req}]
    (let [resp (handler req)]
      (log/info (name request-method) (:status resp)
        (if-let [qs (:query-string req)]
          (str uri "?" qs) uri))
      resp)))

(defn -main [& args]
  (http/run-server (-> #'all-routes site wrap-request-logging) {:port 8080})
  (log/info "Server started at port 8080"))
