(ns matonen.util)

(def pi2 (* Math/PI 2))

(defn get-time []
  #+cljs (.getTime (js/Date.))
  #+clj  (System/currentTimeMillis))

(defn rgb->color [r g b]
  (str "rgb(" r "," g "," b ")"))

(defn rgba->color [r g b a]
  (str "rgba(" r "," g "," b "," a ")"))

#+cljs
(defn window-size []
  [(.-innerWidth js/window) (.-innerHeight js/window)])

#+cljs
(defn prevent-default [e]
  (.preventDefault e)
  (.stopPropagation e)
  e)
