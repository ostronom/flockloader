(ns flockloader.combine
  (:require [flockloader.fetch :as fetch]
            [clojure.core.async :as async :refer [<! go]]))


; (defn get-2nd-level-domain [s]
;   (let [parts (-> s .toURL .getHost (string/split #"\."))]
;     (->> parts reverse (take 2) reverse (string/join "."))))
; (map get-2nd-level-domain coll) -- if we really want
; to truncate `www.linux.org.ru` to `org.ru`

(defn get-host [s]
  (-> s .toURL .getHost))

(defn combine [coll]
  ;; Traverse `coll` of type LazySeq and construct a map counting occurences
  ;; of each element
  (loop [xs     coll
         result (transient {})]
    (if xs
      (let [x (first xs)]
        (recur (next xs) (assoc! result x (inc (get result x 0)))))
      (persistent! result))))

(defn get-combined-result [fetcher terms]
  ;; here we have set of URLs retrieved from BING combined over terms
  ;; since it is a set, we can assume that the URLs here are unique, so we can
  ;; safely extract hostnames, since map of set produces seq which is can contain
  ;; non-unique elements
  (go (combine (map get-host (<! (fetch/fetch-terms fetcher terms))))))
