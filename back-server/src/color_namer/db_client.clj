(ns color-namer.db-client
  (:require [datomic.client.api :as d]))

(def db-uri "datomic:mem://foo")

(def conn (atom nil))

(def color-name-schema
  [{:db/ident :color/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The name of color"}
   {:db/ident :color/code
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "The code of color"}
   {:db/ident :color/id
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "The id of color"}])


(defn insert-color-names[color-names]
  @(d/transact @conn color-names))

(defn rgb->long[red green blue]
  (+ (* red 65536) (* green 256) blue))

(defn add-sample-colors[]
  (let [colors [{:color/id 1 :color/name "red" :color/code (rgb->long 255 0 0)}
                {:color/id 2 :color/name "blue" :color/code (rgb->long 0 0 255)}]]
    @(d/transact @conn colors)))

(defn add-colors[colors]
  @(d/transact @conn colors))


(defn find-by-color-id[id]
  (let [q `[:find ~'?name, ~'?code
            :where [~'?color :color/name ~'?name] [~'?color :color/id ~id] [~'?color :color/code ~'?code]]]
    (print q)
    (d/q q (d/db @conn))))

(defn find-all[]
  (let [q '[:find ?name, ?code, ?id
            :where [?color :color/name ?name] [?color :color/id ?id] [?color :color/code ?code]]]
    (d/q q (d/db @conn))))
  

(defn init[]
  (d/create-database db-uri)
  (reset! conn (d/connect db-uri))
  @(d/transact @conn color-name-schema))