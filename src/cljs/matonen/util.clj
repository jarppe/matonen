(ns matonen.util)

(defmacro with-ctx [ctx & body]
  `(doto ~ctx
     (.save)
     ~@body
     (.restore)))
