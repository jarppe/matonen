(ns matonen.render-game
  (:require [matonen.util :as u :refer-macros [with-ctx]]
            [matonen.anim :as a]))

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

(defn render-apples [{:keys [ctx apples ts]}]
  (aset ctx "strokeStyle" (u/rgb->color 32 255 32))
  (doseq [{:keys [x y r a]} apples]
    (doto ctx
      (aset "fillStyle" (u/rgba->color 32 255 32 (a ts)))
      (.beginPath)
      (.arc x y r 0 u/pi2 false)
      (.fill)
      (.stroke))))

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
