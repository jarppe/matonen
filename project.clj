(defproject matonen "0.1.0-SNAPSHOT"
  :description "Matonen, simple game experiment with ClojureScript"
  :url "http://github.com/jarppe/matonen"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.5.0"
  
  :dependencies [[org.clojure/clojure "1.6.0"]
                 
                 ; server:
                 [compojure "1.2.2"]
                 [ring/ring-core "1.3.2"]
                 [http-kit "2.1.19-SNAPSHOT"]
                 [javax.servlet/servlet-api "2.5"]
                 
                 ; client:
                 [org.clojure/clojurescript "0.0-2356"]
                 [prismatic/dommy "1.0.0"]
                 [alandipert/storage-atom "1.2.3"]
                 [lively "0.1.2"]]
  
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-midje "3.0.0"]]
  
  :source-paths ["src/clj"]

  :profiles {:dev {:plugins [[com.keminglabs/cljx "0.4.0" :exclusions [org.clojure/clojure]]]
                   :dependencies [[midje "1.6.3"]]}
             :prod {:cljsbuild {:builds {:client {:source-paths ^:replace ["src/cljs" "target/cljs"]
                                                  :compiler {:output-to      "./matonen.js"
                                                             :optimizations  :simple
                                                             :pretty-print   false}}}}}
             :uberjar {:main matonen.server
                       :aot [matonen.server]
                       :uberjar-name "matonen.jar"}}
  
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}
                  {:source-paths ["src/cljx"]
                   :output-path "target/cljs"
                   :rules :cljs}]}
  
  :cljsbuild {:builds {:client {:source-paths ["src/cljs" "target/cljs" "src/cljs-dev"]
                                :compiler {:output-to      "./matonen-dev.js"
                                           :output-dir     "./out"
                                           :optimizations  :none
                                           :source-map     "./matonen.js.map"}}}}
  
  :aliases {"cljs" ["do" ["cljsbuild" "clean"] ["cljsbuild" "auto"]]
            "build" ["do" ["cljx" "once"] ["cljsbuild" "clean"] ["with-profile" "prod" "cljsbuild" "once"]]})
