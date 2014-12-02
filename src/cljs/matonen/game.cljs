(ns matonen.game
  (:require [matonen.util :as u :refer-macros [with-ctx]]
            [matonen.anim :as a]))

(def velocity 2.0)
(def max-apples 10)
(def mato-len 100)

(defn init [game]
  (assoc game
         :mato {:path '([0 0])
                :dir  0}
         :apples nil
         :scores nil))

(defn update-apple [{apples :apples :as game}]
  (if (and (< (count apples) max-apples) (< (rand) 0.02))
    (let [width   (:width game)
          height  (:height game)
          x       (- (rand width) (/ width 2.0))
          y       (- (rand height) (/ height 2.0))
          r       (+ (rand 30) 10)]
      (update-in game [:apples] #(cons [x y r] %)))
    game))

(defn update-mato [{{:keys [path dir]} :mato :keys [orientation orient?] :as game}]
  (let [dir     (+ dir (cond
                         orient?        (/ orientation 10.0) 
                         (:left game)   -0.08
                         (:right game)  0.08
                         :else          0))
        [[x y]] path
        y       (- y (* (Math/cos dir) velocity))
        x       (+ x (* (Math/sin dir) velocity))]
    (update-in game [:mato] assoc
               :path (cons [x y] (take mato-len path))
               :dir dir)))

(defn apple->score [ts [x y r]]
  (let [points (Math/round (- 500 (* r 10)))]
    {:x x
     :y y
     :points points
     :size (a/scale 5 100 1000 ts)
     :alpha (a/scale 1 0 1000 ts)}))

(defn eat-apple? [mx my [ax ay ar]]
  (let [dx  (- mx ax)
        dy  (- my ay)
        d   (Math/sqrt (+ (* dx dx) (* dy dy)))]
    (< d ar)))

(defn eat-apples [{:keys [mato apples] :as game}]
  (let [[[x y]] (:path mato)
        eaten   (filter (partial eat-apple? x y) apples)]
    (if (seq eaten)
      (-> game
          (update-in [:scores] concat (map (partial apple->score (:ts game)) eaten))
          (assoc :apples (remove (partial eat-apple? x y) apples)))
      game)))

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
      (eat-apples)
      (crash-check)))

(defn render-apples [{:keys [ctx apples]}]
  (aset ctx "strokeStyle" (u/rgb->color 32 255 32))
  (doseq [[x y r] apples]
    (doto ctx
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
    (render-apples)
    (render-scores))
  (.restore ctx)
  game)
