(ns color-namer.core
  (:require
   [reagent.dom :as rdom]
   [color-namer.canvas :as canvas]))


(defn home []
  [:div
   [:h3 {:class "bg-white dark:bg-black"} "canvas"]
   [canvas/canvas]])


(defn ^:export init
  []
  (rdom/render
   [home]
   (.getElementById js/document "app")))
