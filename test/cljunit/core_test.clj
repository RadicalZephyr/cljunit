(ns cljunit.core-test
  (:require [cljunit.core :as sut]
            [midje.sweet :as midje]
            [midje.sweet :as m]))

(m/facts "about running tests"
  (with-out-str
    (m/fact "it can filter test runs based on package names"
      (sut/run-tests-in-classes ["cljunit.CljUnitTest"]
                                :packages ["other"]) => nil)

    (m/fact "it can find and run a specificied class with tests"
      (sut/run-tests-in-classes ["cljunit.CljUnitTest"]
                                :classes ["CljUnitTest"]) => {:failures 0})

    (m/fact "it can find and run classes in a specified package"
      (sut/run-tests-in-classes ["cljunit.CljUnitTest"]
                                :packgaes ["cljunit"]) => {:failures 0})

    (m/fact "it can run a class that is a jUnit test suite"
      (sut/run-tests-in-classes ["cljunit.CljUnitPassingSuite"]
                                :classes ["CljUnitPassingSuite"]) => {:failures 0})

    (m/fact "it can run a jUnit 3 style TestCase"
      (sut/run-tests-in-classes ["cljunit.CljUnitV3Test"]
                                :packages ["cljunit"]) => {:failures 0})))
