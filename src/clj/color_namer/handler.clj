(ns color-namer.handler
  (:require
   [reitit.ring :as reitit-ring]
   [color-namer.middleware :refer [middleware]]
   [color-namer.db-client :as db-client]
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]
   [clojure.edn :as edn]))

(def db {:dbtype "mysql" :dbname "color_namer"
         :user "jaeyeon" :password "12345678" :host "localhost"})


(def mount-target
  [:div#app
   [:h2 "Welcome to color-namer"]
   [:p "please wait while Figwheel/shadow-cljs is waking up ..."]
   [:p "(Check the js console for hints if nothing exciting happens.)"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    (include-js "/js/app.js")
    [:script "color_namer.core.init_BANG_()"]]))


(defn index-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

(defn add-color-handler
  [_request]
  (println _request)
  {:status 200
   :headers {"Content-Type" "application/edn"}
   :body (db-client/add-colors (:body _request))})

(defn get-color-handler
  [_request]
  (println _request)
  {:status 200
   :headers {"Content-Type" "application/edn"}
   :body (str (db-client/find-all))})

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/" {:get {:handler index-handler}}]
     ["/color"
      ["/register" {:post {:handler add-color-handler}}]
      ["/find/"
       ["index/:item-id" {:get {:handler get-color-handler
                          :parameters {:path {:item-id int?}}}}]
       ["all" {:get {:handler get-color-handler}}]]]
     ["/about" {:get {:handler index-handler}}]])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware middleware}))
