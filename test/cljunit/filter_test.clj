(ns cljunit.filter-test
  (:require [cljunit.filter :as sut]
            [midje.sweet :as m]))

(m/facts "about filter/by"
  (m/fact "it is robust against nils"
    ((sut/by {:classes [nil]}) "Thing") => m/truthy
    ((sut/by {:packages [nil]}) "Thing") => m/truthy)

  (m/fact "it can filter by class name"
    ((sut/by {:classes ["a.p.ShortName"]}) "ShortName") => m/falsey
    ((sut/by {:classes ["ShortName"]}) "DifferentName") => m/falsey
    ((sut/by {:classes ["ShortName"]}) "a.packaged.DifferentName") => m/falsey

    ((sut/by {:classes ["ShortName"]}) "ShortName") => m/truthy
    ((sut/by {:classes ["a.p.ShortName"]}) "a.p.ShortName") => m/truthy
    ((sut/by {:classes ["ShortName"]}) "a.packaged.ShortName") => m/truthy)

  (m/fact "it can filter by package names"
    ((sut/by {:packages ["b"]}) "a.AnyClass") => m/falsey
    ((sut/by {:packages ["b"]}) "a.b.AnyClass") => m/falsey
    ((sut/by {:packages ["a.b"]}) "a.AnyClass") => m/falsey

    ((sut/by {:packages ["a"]}) "a.AnyClass") => m/truthy
    ((sut/by {:packages ["a"]}) "a.b.AnyClass") => m/truthy
    ((sut/by {:packages ["a.b"]}) "a.b.AnyClass") => m/truthy))
