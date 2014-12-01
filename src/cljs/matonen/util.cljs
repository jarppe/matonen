(ns matonen.util)

(def pi2 (* Math/PI 2))

(defn get-time []
  (.getTime (js/Date.)))

(defn window-size []
  [(.-innerWidth js/window) (.-innerHeight js/window)])

(defn prevent-default [e]
  (.preventDefault e)
  (.stopPropagation e)
  e)
