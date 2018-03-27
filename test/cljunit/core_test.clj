(ns cljunit.core-test
  (:require [cljunit.core :as sut]
            [midje.sweet :as m])
  (:import cljunit.ListenerFixture))

(m/facts "about running tests"
  (with-out-str
    (m/fact "it gracefully handles being given non-existent class names"
      (sut/run-tests-in-classes ["foo.bar.Baz"]
                                :packages ["other"]) => nil)

    (m/fact "it can filter test runs based on package names"
      (sut/run-tests-in-classes ["cljunit.CljUnitTest"]
                                :packages ["other"]) => nil)

    (m/fact "it can find and run a specificied class with tests"
      (sut/run-tests-in-classes ["cljunit.CljUnitTest"]
                                :classes ["CljUnitTest"]) => {:failures 0})

    (m/fact "it can find and run classes in a specified package"
      (sut/run-tests-in-classes ["cljunit.CljUnitTest"]
                                :packages ["cljunit"]) => {:failures 0})

    (m/fact "it can run a class that is a jUnit test suite"
      (sut/run-tests-in-classes ["cljunit.CljUnitPassingSuite"]
                                :classes ["CljUnitPassingSuite"]) => {:failures 0})

    (m/fact "it can run a jUnit 3 style TestCase"
      (sut/run-tests-in-classes ["cljunit.CljUnitV3Test"]
                                :packages ["cljunit"]) => {:failures 0})

    (m/facts "adding arbitrary other RunListeners"
      (m/fact "it can take RunListeners passed by name"
        (sut/run-tests-in-classes ["cljunit.CljUnitTest"]
                                  :listeners ["cljunit.ListenerFixture"]
                                  :packages ["cljunit"]) => {:failures 0}
        (ListenerFixture/runStartedCalled) => m/truthy
        (ListenerFixture/runFinishedCalled) => m/truthy

        (m/against-background (m/before :facts (ListenerFixture/reset))))

      (m/fact "it ignores class names that are not RunListeners"
        (sut/run-tests-in-classes ["cljunit.CljUnitTest"]
                                  :listeners ["cljunit.NonRunListener"]
                                  :packages ["cljunit"]) => {:failures 0}))))
