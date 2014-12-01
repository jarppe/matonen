(ns matonen.dev
  (:require [lively :as l]))

(js/console.log "dev setup")
(l/start "/matonen-dev.js" {:polling-rate 500 :on-reload (fn [] (js/console.log "reloaded"))})
