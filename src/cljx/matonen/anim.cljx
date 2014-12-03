(ns matonen.anim)

(defn scale [from to ms start-time]
  (let [diff-ms (/ (- to from) ms)]
    (fn [now]
      (let [diff (- now start-time)]
        (when (>= ms diff)
          (+ from (* diff diff-ms)))))))
