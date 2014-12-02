(ns matonen.app
  (:require [dommy.core :as d]
            [matonen.util :as u]
            [matonen.game :as g]))

(defonce game-state (atom {}))

(def schedule (or (.-requestAnimationFrame js/window)
                  (.-mozRequestAnimationFrame js/window)
                  (.-webkitRequestAnimationFrame js/window)
                  (.-msRequestAnimationFrame js/window)
                  (fn [f] (.setTimeout js/window f 16))))

(defn tick [{:keys [tick paused?] :as game}]
  (if paused?
    game
    (assoc game
           :tick  (inc tick)
           :ts    (u/get-time))))

(defn update [{:keys [paused? crash?] :as game}]
  (if (or paused? crash?)
    game
    (g/update game)))

(defn run []
  (schedule run)
  (->> @game-state
       (tick)
       (update)
       (g/render)
       (reset! game-state)))

(defn reset-game! []
  (js/console.log "reset-game!")
  (let [canvas          (js/document.getElementById "game")
        [width height]  (u/window-size)]
    (doto canvas
      (aset "width" width)
      (aset "height" height))
    (reset! game-state (g/init {:canvas   canvas
                                :ctx      (.getContext canvas "2d")
                                :width    width
                                :height   height
                                :hw       (/ width 2.0)
                                :hh       (/ height 2.0)
                                :tick     0
                                :ts       (u/get-time)
                                :orient?  true
                                :paused?  false
                                :crash?   false
                                :score    0}))))

(defn pause-game []
  (if (:crash? @game-state)
    (reset-game!)
    (swap! game-state update-in [:paused?] not)))

(defn click [e]
  (pause-game))

(def keydown {32   pause-game
              120  reset-game!
              37   (fn [] (swap! game-state assoc :left true))
              39   (fn [] (swap! game-state assoc :right true))
              79   (fn [] (swap! game-state update-in [:orient?] not))})
(def keyup {37   (fn [] (swap! game-state assoc :left false))
            39   (fn [] (swap! game-state assoc :right false))})

(defn on-keydown [e]
  (js/console.log "key:" (.-keyCode e))
  (when-let [f (keydown (.-keyCode e))]
    (u/prevent-default e)
    (f)))

(defn on-keyup [e]
  (when-let [f (keyup (.-keyCode e))]
    (u/prevent-default e)
    (f)))

(def deg->rad (/ Math.PI 180.0))

(defn deviceorientation [e]
  (if (:orient? @game-state)
    (swap! game-state assoc
           :orientation (-> e (.-beta) (* deg->rad)))))

(defn init []
  (js/console.log "init")
  (reset-game!)
  (let [canvas (js/document.getElementById "game")]
    (d/listen! canvas :click (comp click u/prevent-default))
    (d/listen! js/document :keydown on-keydown)
    (d/listen! js/document :keyup on-keyup)
    (d/listen! js/window :deviceorientation deviceorientation))
  (run))

(-> js/window .-onload (set! (partial init)))
