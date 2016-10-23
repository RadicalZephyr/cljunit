(ns cljunit.core
  (:require [clojure.pprint :refer [cl-format]]
            [clj-stacktrace.core :refer [parse-exception]]
            [clojure.string :as str]
            [clansi.core    :refer [style]]
            [cljunit.filter :as filter])
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

(defn- skip-assert-traces [traces]
  (filter #(not (or (= (:class %) "org.junit.Assert")
                    (= (:class %) "org.hamcrest.MatcherAssert"))) traces))

(defn- take-until-reflection [traces]
  (take-while #(not (.contains (:class %) "reflect")) traces))

(defn- convert-to-message [trace]
  (format "%s.%s  %s: %d "
          (:class trace)
          (:method trace)
          (:file trace)
          (:line trace)))

(defn- process-trace [throwable]
  (let [ex-map (parse-exception throwable)
        relevant-traces (->> (:trace-elems ex-map)
                             skip-assert-traces
                             take-until-reflection
                             (map convert-to-message))]
    (format "%s: %s\n       %s"
            (:class ex-map)
            (:message ex-map)
            (str/join "\n       " relevant-traces))))


(defn- print-failed-tests [test-failures]
  (when (seq test-failures)
    (println "Failed:")
    (println)
    (doseq [[i failure] (map-indexed vector test-failures)]
      (printf "  %d) %s\n"
              (inc i) (.getTestHeader failure))
      (printf "     %s\n" (style (process-trace (.getException failure))
                                 :red))
      (println))))

(defn- print-test-summary [result]
  (printf "Finished in %s seconds\n" (float (/ (.getRunTime result) 1000)))
  (let [run-count     (.getRunCount      result)
        ignore-count  (.getIgnoreCount   result)
        failure-count (.getFailureCount  result)
        test-count (+ run-count ignore-count)]
    (println (style (cl-format nil "~D test~:P, ~D failure~:P~[~;, ~:*~D ignored~]~%"
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
  (or (has-annotation? klass org.junit.runner.RunWith)
      (some has-junit-test-annotation? (.getDeclaredMethods klass))))

(defn run-tests-in-classes [class-names & {:as filters}]
  (let [cl (clojure.lang.RT/makeClassLoader)
        test-classes (->> class-names
                          (filter (filter/by filters))
                          (map #(.loadClass cl %))
                          (filter has-junit-tests?))]
    (when (seq test-classes)
      (let [^JUnitCore core (doto (JUnitCore.)
                              (.addListener (run-listener test-classes)))
            result (.run core
                         (into-array Class test-classes))]
        {:failures (.getFailureCount result)}))))
