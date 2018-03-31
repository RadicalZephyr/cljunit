(set-env!
 :source-paths #{"src"}
 :resource-paths #{"src"}
 :dependencies '[[org.clojure/clojure         "1.9.0"]
                 [radicalzephyr/clansi        "1.2.0"]
                 [io.aviso/pretty             "0.1.34"]
                 [junit                       "4.12"]
                 [org.glassfish/javax.servlet "3.0"]
                 [midje                       "1.9.1" :scope "test"]
                 [zilti/boot-midje            "0.2.2-SNAPSHOT" :scope "test"]])

(require '[zilti.boot-midje :refer [midje]])

(def +version+ "0.3.0")

(task-options!
 pom  {:project     'radicalzephyr/cljunit
       :version     +version+
       :description "Run jUnit tests from clojure"
       :url         "https://github.com/radicalzephyr/cljunit"
       :scm         {:url "https://github.com/radicalzephyr/cljunit"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

;;; This prevents a name collision WARNING between the test task and
;;; clojure.core/test, a function that nobody really uses or cares
;;; about.
(ns-unmap 'boot.user 'test)

(deftask test
  "Run the tests"
  []
  (set-env! :source-paths #(conj % "test"))
  (comp (javac) (midje)))
