(ns matonen.anim-test
  (:require [midje.sweet :refer :all]
            [matonen.anim :refer :all]))

(let [s (scale 0 10 100 1000)]
  (facts
    (s 1000) => 0
    (s 1050) => 5
    (s 1100) => 10
    (s 1101) => nil))

(let [s (scale 10 0 100 1000)]
  (facts
    (s 1000) => 10
    (s 1050) => 5
    (s 1100) => 0
    (s 1101) => nil))
