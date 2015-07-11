(ns cljunit.core-test
  (:require [cljunit.core :refer :all]
            [midje.sweet  :refer :all]))

(facts "about running tests"
  (facts "about get-class-in-package"
    (get-class-in-package "java.io" "TestingClass") => nil
    (get-class-in-package "java.io" "File")         => java.io.File)

  (facts "about find-class-in-packages"
    (find-class-in-packages ["java.io"] "TestingClass") => nil
    (find-class-in-packages ["java.io"] "File") => java.io.File
    (find-class-in-packages ["java.net" "java.io"] "File") => java.io.File)

  (facts "about run-test-class"
    (with-out-str
      (run-test-class ["java.net"] "TestingClass") => nil
      (run-test-class ["cljunit"]
                      "CljUnitPassingSuite") => {:failures 0}
      (run-test-class ["failing.cljunit"]
                      "CljUnitFailingSuite") => {:failures 1}))


  (facts "about run-tests-in-packages"
    (with-out-str
      (run-tests-in-packages ["failing.cljunit"]) => {:failures 1}
      (run-tests-in-packages ["cljunit"]) => {:failures 0})))
