(ns cljunit.core-test
  (:require [cljunit.core :refer :all]
            [clojure.test :refer :all]))

(testing "run-tests-in-packages"
  (with-out-str
    (is (= {:failures 0} (run-tests-in-packages ["cljunit"])))))
