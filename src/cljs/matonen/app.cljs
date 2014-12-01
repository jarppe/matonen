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

(defn update [{:keys [paused?] :as game}]
  (if paused?
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
                                :tick     0
                                :ts       (u/get-time)
                                :paused?  false
                                :score    0}))))

(defn click [e]
  (js/console.log "click:" e))

(def CR     13)
(def SPACE  32)
(def X      120)

(defn keypress [e]
  (condp = (.-keyCode e)
    CR     (js/console.log (pr-str (:mato @game-state)))
    SPACE  (swap! game-state update-in [:paused?] not)
    X      (reset-game!)
    nil))

(def deg->rad (/ Math.PI 180.0))

(defn deviceorientation [e]
  (swap! game-state assoc
         :orientation (-> e (.-gamma) (* deg->rad))))

(defn init []
  (js/console.log "init")
  (reset-game!)
  (let [canvas (js/document.getElementById "game")]
    (d/listen! canvas :click (comp click u/prevent-default))
    (d/listen! js/document :keypress (comp keypress u/prevent-default))
    (d/listen! js/window :deviceorientation deviceorientation))
  (run))

(-> js/window .-onload (set! (partial init)))
