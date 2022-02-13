(ns color-namer.middleware
  (:require [clojure.tools.logging :as log]))

(defn middleware
  [handler]
  (fn [request]
    (log/debug "call middleware request : " request)
    (handler request)))