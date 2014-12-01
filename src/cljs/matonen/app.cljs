(ns matonen.app
  (:require [matonen.game :as g]))

(def schedule (or (.-requestAnimationFrame js/window)
                  (.-mozRequestAnimationFrame js/window)
                  (.-webkitRequestAnimationFrame js/window)
                  (.-msRequestAnimationFrame js/window)
                  (fn [f] (.setTimeout js/window f 16))))

(defn run []
  (schedule run)
  (g/step))

(-> js/window .-onload (set! (partial g/init run)))
