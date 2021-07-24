(ns color-namer.server
    (:require
     [color-namer.handler :refer [app]]
     [color-namer.db-client :as db]
     [config.core :refer [env]]
     [ring.adapter.jetty :refer [run-jetty]]
     )
    (:gen-class))


(defn -main [& args]
  (let [port (or (env :port) 3000)]
    (db/init)
    (run-jetty #'app {:port port :join? false})))
