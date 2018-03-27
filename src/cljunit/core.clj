(ns cljunit.core
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [clansi.core    :refer [style]]
            [cljunit.filter :as filter]
            [io.aviso.exception :as e])
  (:import org.junit.runner.JUnitCore
           org.junit.runner.notification.RunListener))

(defn- print-ignored-tests [ignored-tests]
  (when (seq ignored-tests)
    (println "Ignored:")
    (println)
    (doseq [[i ignored] (map-indexed vector ignored-tests)]
      (printf "  %d) %s\n"
              (inc i) (style (.getDisplayName ignored) :yellow))
      (println))))

(def junit-frame-rules
  [[#(str (:class %) "." (:method %)) "java.lang.Thread.run" :hide]
   [:package "java.lang.reflect" :hide]
   [:package "java.util.concurrent" :hide]
   [:package #"sun\.reflect.*" :hide]

   [:package "clojure.lang" :hide]
   [:package "clojure.core" :hide]
   [:name #"clojure\.core.*" :hide]

   [:package #"boot.*" :hide]
   [:package "org.projectodd.shimdandy.impl" :hide]
   [:simple-class #"pod.*" :hide]

   [:package #"org\.junit.*" :hide]
   [:name #"cljunit\.core.*" :hide]
   [:name #"radicalzephyr\.boot-junit.*" :hide]

   [:class "org.junit.Assert" :terminate]
   [:class "org.hamcrest.MatcherAssert" :terminate]])

(defn- print-failed-tests [test-failures]
  (when (seq test-failures)
    (println "Failed:")
    (println)
    (binding [io.aviso.exception/*default-frame-rules* junit-frame-rules]
      (doseq [[i failure] (map-indexed vector test-failures)]
        (printf "  %d) %s\n" (inc i) (.getTestHeader failure))
        (e/write-exception *out* (.getException failure))
        (println)))))

(defn- print-test-summary [result]
  (printf "Finished in %s seconds\n" (float (/ (.getRunTime result) 1000)))
  (let [run-count     (.getRunCount      result)
        ignore-count  (.getIgnoreCount   result)
        failure-count (.getFailureCount  result)
        test-count (+ run-count ignore-count)]
    (println (style (pp/cl-format nil "~D test~:P, ~D failure~:P~[~;, ~:*~D ignored~]~%"
                                  test-count failure-count ignore-count)
                    (if (> failure-count 0) :red :green)))))

(defn run-listener [classes]
  (let [running-tests (atom #{})
        ignored-tests (atom #{})]
    (proxy [RunListener]
        []
      (testRunStarted [description]
        (println "Running jUnit tests for:\n\t"
                 (str/join "\n\t" classes)))

      (testRunFinished [result]
        (print "\n\n")
        (print-ignored-tests @ignored-tests)
        (print-failed-tests (.getFailures result))
        (print-test-summary result))

      (testStarted [description]
        (swap! running-tests conj description))

      (testIgnored [description]
        (swap! ignored-tests conj description)
        (print (style "*" :yellow)))

      (testFinished [description]
        (when (@running-tests description)
          (swap! running-tests disj description)
          (print (style "." :green))))

      (testFailure [failure]
        (let [description (.getDescription failure)]
          (when (@running-tests description)
            (swap! running-tests disj description)
            (print (style "F" :red))))))))

(defn- has-annotation? [subject annotation-type]
  (some #(= annotation-type (.annotationType %))
        (.getDeclaredAnnotations subject)))

(defn- has-junit-test-annotation? [method]
  (has-annotation? method org.junit.Test))

(defn- has-junit-tests? [klass]
  (or (isa? klass junit.framework.TestCase)
      (has-annotation? klass org.junit.runner.RunWith)
      (some has-junit-test-annotation? (.getDeclaredMethods klass))))

(defn run-tests-in-classes [class-names & {:as options}]
  (let [filters (dissoc options :listeners)
        listeners (get options :listeners)
        cl (clojure.lang.RT/makeClassLoader)
        test-classes (->> class-names
                          (filter (filter/by filters))
                          (map #(.loadClass cl %))
                          (filter has-junit-tests?))]
    (when (seq test-classes)
      (let [^JUnitCore core (doto (JUnitCore.)
                              (.addListener (run-listener test-classes)))
            _ (doseq [listener-name listeners]
                (let [klass (Class/forName listener-name)]
                  (when (isa? klass RunListener)
                    (let [constr (.getConstructor klass (into-array Class []))
                          ^RunListener obj (.newInstance constr (into-array Object []))]
                      (.addListener core obj)))))
            result (.run core
                         (into-array Class test-classes))]
        {:failures (.getFailureCount result)}))))
