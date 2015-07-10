(ns cljunit.core-test
  (:require [cljunit.core :refer :all]
            [clojure.test :refer :all]))

(testing "get-class-in-package"
  (is (= nil          (get-class-in-package "java.io" "TestingClass")))
  (is (= java.io.File (get-class-in-package "java.io" "File"))))

(testing "get-suite-class"
  (is (= nil          (find-class-in-packages ["java.io"] "TestingClass")))
  (is (= java.io.File (find-class-in-packages ["java.io"] "File")))
  (is (= java.io.File (find-class-in-packages ["java.net" "java.io"] "File"))))

(testing "run-test-suite"
  (with-out-str
    (is (= nil (run-test-suite ["java.net"] "TestingClass")))
    (is (= {:failures 0} (run-test-suite ["cljunit"] "CljUnitPassingSuite")))))

(testing "run-tests-in-packages"
  (with-out-str
    (is (= {:failures 0} (run-tests-in-packages ["cljunit"])))))
