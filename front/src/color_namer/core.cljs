(ns color-namer.core
  (:require
   [color-namer.router :as router]
   [reagent.dom :as rdom]
   [reagent.core :as r]
   [color-namer.canvas :as canvas]
   [reitit.frontend.easy :as rfe]
   [reitit.frontend :as rf]
   [reitit.coercion.spec :as rcs]))

(defonce match (r/atom nil))

(defn home []
  [:div
   [:h3 {:class "bg-white dark:bg-black"} "canvas"]
   [canvas/canvas]])



(defn ^:export main
  []
  (rfe/start!
   (rf/router router/routes {:data {:coercion rcs/coercion}})
   (fn [m] (reset! match m))
   {:use-fragment true})
  
  (rdom/render
   [home]
   (.getElementById js/document "app")))

