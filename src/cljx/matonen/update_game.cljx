(ns matonen.update-game
  (:require [matonen.util :as u]
            [matonen.anim :as a]))

(def velocity 2.0)
(def max-apples 10)
(def mato-len 100)

(defn consf [v s] (cons s v))

(defn init [game]
  (assoc game
         :mato {:path '([0 0])
                :dir  0}
         :apples nil
         :scores nil))

(defn apple->score [ts {:keys [x y points]}]
  {:x x
   :y y
   :points points
   :size (a/scale 5 100 1000 ts)
   :alpha (a/scale 1 0 1000 ts)})

(defn eat-apple? [mx my {:keys [x y r] :as apple}]
  (let [dx  (- mx x)
        dy  (- my y)
        d   (Math/sqrt (+ (* dx dx) (* dy dy)))]
    (if (< d r)
      apple)))

(defn eat-apple [{:keys [ts apples] {[[x y]] :path} :mato :as game}]
  (if-let [eaten (some (partial eat-apple? x y) apples)]
    (-> game
        (update-in [:apples] (partial remove (partial identical? eaten)))
        (update-in [:score] + (:points eaten))
        (update-in [:scores] consf (apple->score ts eaten)))
    game))

(defn old-apple? [ts {a :a :as apple}]
  (if (nil? (a ts))
    apple))

(defn drop-old-apple [{:keys [ts apples] :as game}]
  (if-let [dead (some (partial old-apple? ts) apples)]
    (assoc game :apples (remove (partial identical? dead) apples))
    game))

(defn new-apple [{:keys [width height ts]}]
  (let [r (+ (rand 30) 10)]
    {:x       (- (rand width) (/ width 2.0))
     :y       (- (rand height) (/ height 2.0))
     :r       r
     :points  (Math/floor (- 500 (* r 10)))
     :a       (a/scale 1 0 5000 ts)}))

(defn add-new-apple [{:keys [apples] :as game}]
  (if (and (< (count apples) max-apples) (< (rand) 0.02))
    (update-in game [:apples] consf (new-apple game))
    game))

(defn update-mato [{:keys [orientation orient?] {:keys [path dir]} :mato :as game}]
  (let [dir      (+ dir (cond
                          orient?        (/ orientation 10.0) 
                          (:left game)   -0.08
                          (:right game)  0.08
                          :else          0))
        [[x y]]  path
        new-y    (- y (* (Math/cos dir) velocity))
        new-x    (+ x (* (Math/sin dir) velocity))]
    (update-in game [:mato] assoc
               :path (cons [new-x new-y] (take mato-len path))
               :dir dir)))

(defn crash-check [{{[[x y]] :path} :mato :keys [hw hh] :as game}]
  (if (and (< (- hw) x hw)
           (< (- hh) y hh))
    game
    (assoc game :crash? true)))

(defn update-tick [{:keys [tick] :as game}]
  (assoc game
         :tick  (inc tick)
         :ts    (u/get-time)))

(defn update [{:keys [paused? crash?] :as game}]
  (if (or paused? crash?)
    game
    (-> game
        (update-tick)
        (drop-old-apple)
        (update-mato)
        (eat-apple)
        (add-new-apple)
        (crash-check))))
