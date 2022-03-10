(ns color-namer.router
  (:require [reitit.core :as r]
            [reitit.frontend :as rf]
            [reitit.coercion.schema :as rsc]
            [schema.core :as s]))

(def routes
  (rf/router
   ["/" {:controllers [{:start (fn [_] (js/console.log "root start"))}]}
    ["items"
     ["/:id"
      {:controllers
       [{:parameters {:path {:id s/Int}}
         :start
         (fn [parameters] (js/console.log :start (-> parameters :path :id)))
         :stop
         (fn [parameters] (js/console.log :stop (-> parameters :path :id)))}]}]]]
   {:data {:coercion rsc/coercion}}))

(def routes-2
  (rf/router
   ["/"
    [""
     {:name ::frontpage
      ;:view home-page
      :controllers [{:start nil #_(log-fn "start" "frontpage controller")
                     :stop nil #_(log-fn "stop" "frontpage controller")}]}]
    ["items"
      ;; Shared data for sub-routes
     {
      ;:view item-page
      :controllers [{:start nil #_(log-fn "start" "items controller")
                     :stop nil #_(log-fn "stop" "items controller")}]}

     [""
      {:name ::item-list
       :controllers [{:start nil #_(log-fn "start" "item-list controller")
                      :stop nil #_(log-fn "stop" "item-list controller")}]}]
     ["/:id"
      {:name ::item
       :parameters {:path {:id s/Int}
                    :query {(s/optional-key :foo) s/Keyword}}
       :controllers [{:parameters {:path [:id]}
                      :start (fn [{:keys [path]}]
                               (js/console.log "start" "item controller" (:id path)))
                      :stop (fn [{:keys [path]}]
                              (js/console.log "stop" "item controller" (:id path)))}]}]]]
   {:data {:controllers [{:start nil #_(log-fn "start" "root-controller")
                          :stop nil #_(log-fn "stop" "root controller")}]
           :coercion rsc/coercion}}))



(comment
  
  (r/match-by-path routes "/items/3"))
