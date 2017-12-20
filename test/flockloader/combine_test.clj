(ns flockloader.combine-test
  (:require [clojure.test :refer :all]
            [flockloader.combine :refer :all]))

(deftest combine-tests
  (testing "Combining of search results"
    (let [res ["a" "b" "c" "d" "a" "b" "f" "b"]]
      (do
        (is (= (combine res) {"a" 2 "b" 3 "c" 1 "d" 1 "f" 1}))))))
