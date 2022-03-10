(ns color-namer.core
  (:require
   [color-namer.router :as router]
   [reagent.dom :as rdom]
   [reagent.core :as r]
   [reitit.core]
   [color-namer.canvas :as canvas]
   [reitit.frontend.easy :as rfe]
   [reitit.frontend.controllers :as rfc]))

(defonce match (r/atom nil))

(defn home []
  [:div
   [:h3 {:class "bg-white dark:bg-black"} "canvas"]
   [canvas/canvas]
   [:a {:href "/items/1"} "click item"]
   [:br]
   [:input {:id "home"
            :type "button"
            :onClick (fn [e]
                       (js/console.log e)
                       (rfe/push-state "item" "/items/1"))
            :value "Click item"}]])


(defn ^:export main
  []
  (rfe/start!
   router/routes
   (fn [new-match]
     (swap! match (fn [old-match]
                    (js/console.log new-match)
                    (when new-match
                      (assoc new-match :controllers 
                             (rfc/apply-controllers 
                              (:controllers old-match) new-match))))))
   {:use-fragment false})
  (rdom/render
   [home]
   (.getElementById js/document "app")))


#_(reitit.core/match-by-path router/routes "/items/3")