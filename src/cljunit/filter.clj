(ns cljunit.filter
  (:require [clojure.string :as str]))

(defn- short-name [class-name]
  (some-> class-name
          (str/split #"\.")
          last))

(defn- package-parts [class-name]
  (some-> class-name
          (str/split #"\.")
          butlast))

(defn- all-nested-packages-seq-itr [current-package package-parts]
  (lazy-seq
   (when (seq package-parts)
     (let [next-package (format "%s.%s" current-package (first package-parts))]
       (cons next-package
             (all-nested-packages-seq-itr next-package (rest package-parts)))))))

(defn- all-nested-packages-seq [class-name]
  (let [package-parts (package-parts class-name)]
    (cons (first package-parts)
          (all-nested-packages-seq-itr (first package-parts)
                                       (rest package-parts)))))

(defn- any-filters-present? [{:keys [classes packages]}]
  (or (seq (remove nil? classes))
      (seq (remove nil? packages))))

(defn by [{:keys [classes packages] :as filters}]
  (let [classes (set classes)
        packages (set packages)]
    (if (any-filters-present? filters)
      (fn [class-name]
        (let [short-name (short-name class-name)
              all-nested-packages (all-nested-packages-seq class-name)]
          (or (contains? classes class-name)
              (contains? classes short-name)
              (some #(contains? packages %) all-nested-packages))))

      (constantly true))))
