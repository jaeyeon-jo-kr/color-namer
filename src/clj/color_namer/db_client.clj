(ns color-namer.db-client
  (:require [datomic.api :as d])

(def db-uri "datomic:mem://foo")

(def client (d/create-database db-uri))

(def conn (d/connect db-uri))

(def color-name-schema[{:db/ident color-name
                        :db/valueType :db.type/string
                        :db/cardinality :db.cardinality/one
                        :db/doc "The name of color"}

                       {:db/ident color-code
                        :db/valueType :db.type/long
                        :db/cardinality :db.cardinality/one
                        :db/doc "The code of color"}])

(def insert-color-names[color-names]
  @(d/transact conn color-names))

(defn init[]
  @(d/transact conn color-name-schema))
