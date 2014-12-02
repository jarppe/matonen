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
    (assoc game :tick (inc tick))))

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
                                :paused?  false
                                :crash?   false
                                :score    0}))))

(defn pause-game []
  (swap! game-state update-in [:paused?] not))

(defn click [e]
  (if (:crash? @game-state)
    (reset-game!)
    (pause-game)))

(def CR     13)
(def SPACE  32)
(def X      120)

(defn keypress [e]
  (condp = (.-keyCode e)
    CR     (js/console.log (pr-str (:apple @game-state)))
    SPACE  (pause-game)
    X      (reset-game!)
    nil))

(def deg->rad (/ Math.PI 180.0))

(defn deviceorientation [e]
  (swap! game-state assoc
         :orientation (-> e (.-beta) (* deg->rad))))

(defn init []
  (js/console.log "init")
  (reset-game!)
  (let [canvas (js/document.getElementById "game")]
    (d/listen! canvas :click (comp click u/prevent-default))
    (d/listen! js/document :keypress (comp keypress u/prevent-default))
    (d/listen! js/window :deviceorientation deviceorientation))
  (run))

(-> js/window .-onload (set! (partial init)))
