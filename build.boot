(set-env!
 :source-paths #{"src" "test"}
 :resource-paths #{"src"}
 :dependencies '[[org.clojure/clojure         "1.6.0"]
                 [adzerk/boot-test            "1.0.4"]
                 [radicalzephyr/clansi        "1.2.0"]
                 [clj-stacktrace              "0.2.8"]
                 [junit                       "4.12"]
                 [org.reflections/reflections "0.9.10"]
                 [org.glassfish/javax.servlet "3.0"]
                 [radicalzephyr/bootlaces     "0.1.12"]])

(require '[radicalzephyr.bootlaces :refer :all]
         '[adzerk.boot-test])

(def +version+ "0.1.1-SNAPSHOT")

(bootlaces! +version+)

(task-options!
 pom  {:project     'radicalzephyr/cljunit
       :version     +version+
       :description "Run jUnit tests from clojure"
       :url         "https://github.com/radicalzephyr/cljunit"
       :scm         {:url "https://github.com/radicalzephyr/cljunit"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}}
 adzerk.boot-test/test {:namespaces '[cljunit.core-test]})

(deftask test []
  (comp (javac)
        (adzerk.boot-test/test)))
