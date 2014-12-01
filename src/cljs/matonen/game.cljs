(ns matonen.game
  (:require [dommy.core :as d]
            [matonen.state :as s]
            [matonen.util :as u]))

(defn update [game]
  game)

(defn render [game]
  game)

(defn step []
  (->> @s/game
       (update)
       (render)
       (reset! s/game))
  nil)

(defn click [e]
  (js/console.log "click:" e))

(def CR 32)
(def Z 122)
(def X 120)

(defn keypress [e]
  (condp = (.-keyCode e)
    CR  (js/console.log "enter" (pr-str @s/game))
    X   (swap! s/game assoc :objects {})
    nil))

(defn init [ready]
  (let [canvas (js/document.getElementById "game")]
    (reset! s/game {:canvas   canvas
                    :ctx      (.getContext canvas "2d")
                    :tick     0
                    :ts       (u/get-time)
                    :objects  {}
                    :score    0})
    (d/listen! canvas :click (comp click u/prevent-default))
    (d/listen! js/document :keypress (comp keypress u/prevent-default))
    (ready)))
