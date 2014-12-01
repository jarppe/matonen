(ns matonen.game
  (:require [matonen.util :as u :refer-macros [with-ctx]]))

(defn init [{:keys [width height] :as game}]
  (assoc game :mato {:path [[-100 -100]
                            [-50 0]
                            [100 150]]
                     :len  100}))

(defn update [{:keys [mato orientation] :as game}]
  game)

(defn render-orientation [{:keys [ctx width height orientation]}]
  (with-ctx ctx
    (aset "textAlign" "center")
    (aset "textBaseline" "top")
    (aset "font" "18px sans-serif")
    (aset "fillStyle" "rgba(32,255,32,0.4)")
    (.translate (/ width 2.0) (/ height 2.0))
    (.rotate orientation)
    (aset "strokeStyle" (u/rgba->color 255 32 32 0.2))
    (aset "fillStyle" (u/rgba->color 255 32 32 0.08))
    (.beginPath)
    (.moveTo -100 150)
    (.lineTo 0 -200)
    (.lineTo 100 150)
    (.closePath)
    (.stroke)
    (.fill)))

(defn render-path [ctx path]
  (doseq [[x y] path]
    (.lineTo ctx x y)))

(defn render-mato [{:keys [ctx width height mato]}]
  (let [[[x y] & path] (:path mato)]
    (with-ctx ctx
      (aset "strokeStyle" (u/rgb->color 255 32 32))
      (.translate (/ width 2.0) (/ height 2.0))
      (.moveTo x y)
      (render-path path)
      (.stroke))))

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
