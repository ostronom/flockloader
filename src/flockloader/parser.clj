(ns flockloader.parser
  (:require [clojure.data.xml :as xml]))

(defn to-normalized-uri [s]
  (.normalize (java.net.URI. s)))

(defn extract-links [s]
  (->> s xml/parse
       :content first :content ;; contents of rss/channel
       (filter #(= (:tag %) :item))
       (mapcat :content)
       (filter #(= (:tag %) :link))
       (mapcat :content)
       (map to-normalized-uri)
       (into #{})))
