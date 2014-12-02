(ns matonen.game
  (:require [matonen.util :as u :refer-macros [with-ctx]]
            [matonen.anim :as a]))

(def velocity 2.0)

(defn init [game]
  (assoc game :mato {:path '([0 0])
                     :len  100
                     :dir  0}))

(defonce id (atom 0))
(defn next-id [] (swap! id inc))

(defn update-apple [{apple :apple :as game}]
  (if (or apple (> (rand) 0.01))
    game
    (let [width (:width game)
          height (:height game)]
      (assoc game :apple [(- (rand width) (/ width 2.0))
                          (- (rand height) (/ height 2.0))
                          (+ (rand 30) 10)]))))

(defn update-mato [{{:keys [path len dir]} :mato :keys [orientation orient?] :as game}]
  (let [dir     (+ dir (cond
                         orient?        (/ orientation 10.0) 
                         (:left game)   -0.08
                         (:right game)  0.08
                         :else          0))
        [[x y]] path
        y       (- y (* (Math/cos dir) velocity))
        x       (+ x (* (Math/sin dir) velocity))]
    (update-in game [:mato] assoc
               :path (cons [x y] (take len path))
               :dir dir)))

(defn eat-apple [{:keys [mato apple] :as game}]
  (if-not apple
    game
    (let [[[mx my]]  (:path mato)
          [ax ay ar] apple
          dx         (- mx ax)
          dy         (- my ay)
          d          (Math/sqrt (+ (* dx dx) (* dy dy)))]
      (if (< d ar)
        (let [points (Math/round (- 500 (* ar 10)))]
          (-> game
              (dissoc :apple)
              (update-in [:score] + points)
              (update-in [:scores] #(cons {:x ax
                                           :y ay
                                           :points points
                                           :size (a/scale 5 100 1000 (:ts game))
                                           :alpha (a/scale 1 0 1000 (:ts game))} %))))
        game))))

(defn render-score [ctx ts {:keys [x y points size alpha]}]
  (let [s (size ts)
        a (alpha ts)]
    (when s
      (doto ctx
        (aset "font" (str s "px sans-serif"))
        (aset "fillStyle" (str "rgba(32,255,32," a ")"))
        (.fillText (str points) x y))
      true)))

(defn render-scores [{:keys [ctx ts scores] :as game}]
  (when (seq scores)
    (doto ctx
      (aset "textAlign" "center")
      (aset "textBaseline" "center"))
    (assoc game :scores (doall (filter (partial render-score ctx ts) scores)))))

(defn crash-check [{{[[x y]] :path} :mato :keys [hw hh] :as game}]
  (if (and (< (- hw) x hw)
           (< (- hh) y hh))
    game
    (assoc game :crash? true)))

(defn update [game]
  (-> game
      (update-mato)
      (update-apple)
      (eat-apple)
      (crash-check)))

(defn render-apple [{:keys [ctx] [x y r] :apple}]
  (if x
    (doto ctx
      (aset "strokeStyle" (u/rgb->color 32 255 32))
      (.beginPath)
      (.arc x y r 0 u/pi2 false)
      (.fill))))

(defn render-path [ctx path]
  (doseq [[x y] path]
    (.lineTo ctx x y)))

(defn render-mato [{:keys [ctx] {[[x y] & path] :path} :mato}]
  (doto ctx
    (aset "strokeStyle" (u/rgb->color 255 32 32))
    (.beginPath)
    (.moveTo x y)
    (render-path path)
    (.stroke)))

(defn render-orientation [{:keys [ctx orientation mato]}]
  (with-ctx ctx
    (.rotate (/ orientation 2.0))
    (aset "fillStyle" (u/rgba->color 255 32 32 0.04))
    (.beginPath)
    (.moveTo -100 150)
    (.lineTo 0 -200)
    (.lineTo 100 150)
    (.closePath)
    (.fill)))

(defn render-board [{:keys [hw hh ctx score paused?]}]
  (doto ctx
    (aset "textAlign" "center")
    (aset "textBaseline" "top")
    (aset "font" "18px sans-serif")
    (aset "fillStyle" "rgba(32,255,32,0.4)")
    (.fillText "Matonen" hw 2)
    (aset "textAlign" "left")
    (.fillText (str score) 2 2))
  (if paused?
    (doto ctx
      (aset "textAlign" "center")
      (aset "font" "108px sans-serif")
      (aset "fillStyle" "rgba(32,255,32,0.4)")
      (.fillText "paused" hw hh))))

(defn render-clear [{:keys [ctx width height crash?]}]
  (doto ctx
    (aset "fillStyle" (if crash? "rgb(128,32,32)" "rgb(32,32,32)"))
    (.fillRect 0 0 width height)))

(defn render [{:keys [ctx hw hh] :as game}]
  (render-clear game)
  (render-board game)
  (.save ctx)
  (.translate ctx hw hh)
  (doto game
    (render-mato)
    (render-apple)
    (render-scores))
  (.restore ctx)
  game)
