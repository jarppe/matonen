(ns matonen.server
  (:require [org.httpkit.server :as http-kit]
            [compojure.core :refer [defroutes]]
            [compojure.handler :as handler]
            [compojure.route :as route])
  (:gen-class))

(defroutes app-routes
  (route/files "/" {:root "."}))

(defn start-server [port]
  (http-kit/run-server #'app-routes {:port port}))

(defn -main [& [port]]
  (let [port (Integer/parseInt (or port
                                   (System/getProperty "server.port")
                                   (System/getenv "PORT")
                                   "8080"))]
    (start-server port)
    (println "Server listening on" port)))
