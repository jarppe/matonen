(ns matonen.game
  (:require [matonen.util :as u :refer-macros [with-ctx]]))

(defn init [{:keys [width height] :as game}]
  (assoc game :mato {:path '([0 0])
                     :len  100
                     :dir  0}))

(defn bound [v]
  (if (> v 1.2)
    1.2
    (if (< v -1.2)
      -1.2
      v)))

(defn update [{:keys [mato orientation width height] :as game}]
  (let [{:keys [path len dir]} mato
        dir     (+ dir (/ orientation 10.0))
        [[x y]] path
        y       (- y (* (Math/cos dir) 2.0))
        x       (+ x (* (Math/sin dir) 2.0))]
    (assoc game :mato {:path (cons [x y] (take len path))
                        :len len
                        :dir dir})))

(defn render-path [ctx path]
  (doseq [[x y] path]
    (.lineTo ctx x y)))

(defn render-mato [{:keys [ctx width height mato]}]
  (let [[[x y] & path] (:path mato)]
    (with-ctx ctx
      (aset "strokeStyle" (u/rgb->color 255 32 32))
      (.translate (/ width 2.0) (/ height 2.0))
      (.beginPath)
      (.moveTo x y)
      (render-path path)
      (.stroke))))

(defn render-orientation [{:keys [ctx width height orientation mato]}]
  (with-ctx ctx
    (.translate (/ width 2.0) (/ height 2.0))
    (.rotate (/ orientation 2.0))
    (aset "fillStyle" (u/rgba->color 255 32 32 0.04))
    (.beginPath)
    (.moveTo -100 150)
    (.lineTo 0 -200)
    (.lineTo 100 150)
    (.closePath)
    (.fill)))

(defn render-clear [{:keys [width height ctx]}]
  (doto ctx
    (aset "fillStyle" "rgb(32,32,32)")
    (.fillRect 0 0 width height)))

(defn render-board [{:keys [width height ctx score paused?]}]
  (doto ctx
    (aset "textAlign" "center")
    (aset "textBaseline" "top")
    (aset "font" "18px sans-serif")
    (aset "fillStyle" "rgba(32,255,32,0.4)")
    (.fillText "Matonen" (/ width 2.0) 2)
    (aset "textAlign" "left")
    (.fillText (str score) 2 2))
  (if paused?
    (doto ctx
      (aset "textAlign" "center")
      (aset "font" "108px sans-serif")
      (aset "fillStyle" "rgba(32,255,32,0.4)")
      (.fillText "paused" (/ width 2.0) (/ height 2.0)))))

(defn render [game]
  (render-clear game)
  (render-board game)
  (render-mato game)
  (render-orientation game)
  game)
