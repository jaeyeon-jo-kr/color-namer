(ns color-namer.server
    (:require
     [color-namer.handler :refer [app]]
     [config.core :refer [env]]
     [ring.adapter.jetty :refer [run-jetty]])
    (:gen-class))

(comment 
  (def db-uri "datomic:mem://foo")

(d/create-database db-uri)
(def conn (d/connect db-uri))

  )


(defn -main [& args]
  (let [port (or (env :port) 3000)]
    (run-jetty #'app {:port port :join? false})))