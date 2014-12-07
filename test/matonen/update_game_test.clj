(ns matonen.update-game-test
  (:require [midje.sweet :refer :all]
            [matonen.update-game :as ug]))

(fact eat-apple
  (ug/eat-apple {:mato    {:path [[100 200]]}
                 :apples  '({:x 100 :y 200 :r 10 :points 50})
                 :scores  ()
                 :score   0
                 :ts      0}) =>
  (contains {:apples  ()
             :scores  (fn [scores]
                        (fact
                          scores => sequential?
                          (count scores) => 1
                          (first scores) => (contains {:x 100 :y 200 :points 50 :alpha fn? :size fn?})))
             :score   50}))

(fact drop-old-apple
  (let [game {:ts 0
              :apples [{:a (constantly 1)}]}]
    (ug/drop-old-apple game) => game)
  (let [game {:ts 0
              :apples [{:a (constantly 1)} {:a (constantly nil)}]}]
    (-> (ug/drop-old-apple game) :apples count) => 1))
