(ns cljunit.core
  (:require [clojure.pprint :refer [cl-format]]
            [clj-stacktrace.core :refer [parse-exception]]
            [clojure.string :as str]
            [clansi.core    :refer [style]])
  (:import org.junit.runner.JUnitCore
           org.junit.runner.notification.RunListener
           (org.reflections Reflections
                            Configuration)
           (org.reflections.scanners Scanner
                                     TypeAnnotationsScanner
                                     MethodAnnotationsScanner)
           (org.reflections.util ClasspathHelper
                                 ConfigurationBuilder
                                 FilterBuilder)))

(defn- build-package-config [^String package]
  (.. (ConfigurationBuilder.)
      (setUrls (ClasspathHelper/forPackage package (into-array ClassLoader [])))
      (setScanners (into-array Scanner [(TypeAnnotationsScanner.)
                                        (MethodAnnotationsScanner.)]))
      (filterInputsBy (.. (FilterBuilder.)
                          (includePackage (into-array String [package]))))))

(defn- find-tests-in-package [package]
  (let [^Configuration config (build-package-config package)
        reflections (Reflections. config)
        test-methods (.getMethodsAnnotatedWith reflections
                                               org.junit.Test)]
    (map (memfn getDeclaringClass) test-methods)))

(defn- find-all-tests [packages]
  (->> packages
       (map str)
       (mapcat find-tests-in-package)
       set))

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

(defn run-tests-for-classes [classes]
  (let [^JUnitCore core (doto (JUnitCore.)
                          (.addListener (run-listener classes)))
        result (.run core
                     (into-array Class classes))]
    {:failures (.getFailureCount result)}))

(defn get-class-in-package [package class]
  (try
    (Class/forName (str package "." class))
    (catch java.lang.ClassNotFoundException e
      nil)))

(defn find-class-in-packages [packages suite]
  (->> packages
       (some #(get-class-in-package % suite))))

(defn run-test-classes [packages class-names]
  (some->> class-names
           (map (partial find-class-in-packages packages))
           (filter identity)
           seq
           run-tests-for-classes))

(defn run-tests-in-packages [packages]
  (run-tests-for-classes (find-all-tests packages)))

(defn -main [& args])
