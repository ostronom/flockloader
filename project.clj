(defproject flockloader "0.1.0-SNAPSHOT"
  :description "flockloader -- simple fetcher with connection limits"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.465"]
                 [org.clojure/data.xml "0.0.8"]
                 [metosin/jsonista "0.1.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [compojure "1.6.0"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-core "1.6.3"]
                 [http-kit "2.2.0"]]
  :main flockloader.core)
