(ns color-namer.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [reagent.core :as reagent :refer [atom cursor]]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [reagent.format :as format]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk];; Notebook for clojure
   [accountant.core :as accountant];; SPA Simple
   [garden.core :refer [css]]
   [cljs-http.client :as http]
   [cljs.core.async :refer [<! >! chan]]
   [thi.ng.geom.gl.core :as gl]
   [thi.ng.geom.core :as geom]
   [thi.ng.geom.matrix :as mat]
   [thi.ng.geom.sphere :as sph]
   [thi.ng.geom.gl.glmesh :as glmesh]
   [thi.ng.geom.gl.shaders :as shaders]
   [thi.ng.geom.triangle :as tri]
   [thi.ng.geom.gl.camera :as cam]
   [thi.ng.geom.gl.webgl.constants :as glc]
   [thi.ng.geom.gl.webgl.animator :as anim]))

(enable-console-print!)

(def shader-spec
  {:vs "void main() {
          gl_Position = proj * view * vec4(position, 1.0);
       }"
   :fs "void main() {
           gl_FragColor = vec4(0.5, 0.5, 1.0, 1.0);
       }"
   :uniforms {:view       :mat4
              :proj       :mat4}
   :attribs  {:position   :vec3}})

(defonce app-state
  (atom {:canvas
         {:activated
          {:color "#102020"}}}))
                 
(defn long->rgb [code]
  [(quot code 65536) (mod (quot code 256) 256) (mod code 256)])

(defn long->rgb-str[code]
  (str "#"
       (-> (.toString code 16)
           (.padStart 6 0))))
  
(defn rgb-str->long[str]
  (-> (clojure.string/replace str #"#" "16r")
      (cljs.reader/read-string)))

;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :index]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

;; -------------------------
;; Page components


(defn send-input [input]
  (prn input)
  (http/post "http://localhost:3000/color/register" {:edn-params input}))

(def activated-color-cursor
  (cursor app-state [:canvas :activated :color]))


(defn color-picker-component
  []
  (let [component-state (atom {})]
   (fn [] 
     [:div {:id "color-picker-component"}
      [:h4 {:style {:padding "20px"
                    :background-color @activated-color-cursor
                    :font-color "white"}}
       "Color picked : " @activated-color-cursor]
      [:input
       {:type "color"
        :on-change
        (fn [e]
          (swap! app-state update-in
                 [:canvas :activated :color]
                 (fn [_] (.. e -target -value))))}]])))

(def color-info (atom []))

(defn load-colors-info
  [colors-info]
  (go (let [response (<! (http/get "http://localhost:3000/color/find/all"))]
        (reset! colors-info (:body response))))
  colors-info)

(defn combine-model-shader-and-camera
  [context model shader-spec camera]
  (-> model
      (gl/as-gl-buffer-spec {})
      (assoc :shader (shaders/make-shader-from-spec context shader-spec))
      (gl/make-buffers-in-spec context glc/static-draw)
      (cam/apply camera)))

(defn draw
  [context model shader-spec camera]
  (set! (.-fillStyle context)
        @activated-color-cursor)
  (comment .fillRect context 0 0 150 150)
  (doto context
    (gl/clear-color-and-depth-buffer 0 0 0 1 1)
    (gl/draw-with-shader (combine-model-shader-and-camera context model shader-spec camera))))

(defn canvas []
  (let [component-status
        (atom {:status {}
               :camera (cam/perspective-camera {})
               :shader-spec
               {:vs "void main() {gl_Position = proj * view * vec4(position, 1.0);}"
                :fs "void main() {gl_FragColor = vec4(0.5, 0.5, 1.0, 1.0);}"
                :uniforms {:view       :mat4
                           :proj       :mat4}
                :attribs  {:position   :vec3}}
               :triangle (geom/as-mesh (tri/triangle3 [[1 0 0] [0 0 0] [0 1 0]])
                                       {:mesh (glmesh/gl-mesh 3)})})]
    (fn []
      (let [{:keys [shader-spec camera triangle]} @component-status]
        [:div {:id "canvas-dev"}
         [:canvas
          {:id "main"
           :width 100
           :height 100
           :onLoad (fn [e]
                     (.log js/console "load canvas")
                     (swap! component-status update-in [:status :context]
                            (fn [_] (gl/gl-context e)))) 
           :onClick
           (fn [c]
             (->> (-> c .-target (.getContext "webgl")
                      (draw triangle shader-spec camera))
                  ))}]]))))

(defn update-name [color-info uid value]
  (map (fn [[name code id]]
         (if (= uid id)
           [value code id]
           [name code id])) color-info))

(defn update-code [color-info uid value]
  (map (fn [[name code id]]
         (if (= uid id)
           [name value id]
           [name code id])) color-info))
  


(defn home-page []
  (fn []
    [:span.main
     [:h1 "Welcome to color-namer"]]))

;; -------------------------
;; Page mounting component

(defn current-page []
  (let [page (:current-page (session/get :route))
        page-info (atom "#000000")]
    (fn []
      [:div
       [:header
       [:h3 (str "current page name : " page)]
       [:footer
        [:p "This project was generated by the "
         [:a {:href "https://github.com/reagent-project/reagent-template"}
          "Reagent Template"] "."]]]])))

(defn color-input-cell 
  [[id name code]]
  [:div {:key (str "color-input-cell-" id)}
    [:h5 (str id "\t" name "\t" code)]
   [:input {:type  "color"
            :id (str "color-picker-" id)
            :name (str "color-" id)
            :value (str code)
            :on-change #(swap! color-info update-code  id
                               (-> % .-target .-value rgb-str->long))}]
   [:input {:type "text" :id "input" :value name
            :on-change #(swap! color-info update-name  id
                               (-> % .-target .-value))}]])


(defn color-input-component []
  (let [component-state (atom {:color-info [[1 "blue" "#FF00FF"]]})]
    (fn []
      (let []
        [:div
         {:id "color-input-component"}
         (->> @component-state
              :color-info
              (map color-input-cell))
         [:button {:type "button" :id "load"
                   :on-click #(load-colors-info color-info)}
          " load cells"]]))))
(defn sand-box []
  (let [component-state
        (atom {:box1 {:style {:width "min-content"
                              :height "10px"
                              :padding "20px"
                              :border "1px solid"}
                      :data-sizing "intrinsic"}})]
    (fn []
       [:div
       [:p @component-state
        "I am a paragraph of text that has a few words in it."]
        [:p (get-in @component-state [:style :data-sizing])]
        [:button
         {:value "click to change box."
          :on-click
          (fn [e]
            (case (get-in @component-state [:box1 :data-sizing])
              "intrinsic"
              (do 
                (swap! component-state assoc-in [:box1 :data-sizing] "extrinsic"))
              "extrinsic"
              (do
                (swap! component-state assoc-in [:box1 :data-sizing] "intrinsic")
                (swap! component-state assoc-in [:box1 :style :width] "min-content")
                (swap! component-state assoc-in [:box1 :style :height] "min-content"))))}
         " change style"]
        [:p {:style {:width "200px"
                     :border "10px solid"
                     :padding "20px"}} "?"]
        [:p {:style {:width "200px"
                     :border "1px solid"}} "?"]
        [:article 
         [:p {:style {:color "red" :font-size "1.5em"}} "abc"]
         [:p "bcd"]]
             
        ])))

(defn home []
  [:div
   [:h3 "color input component"]
   [color-input-component]
   [:h3 "canvas"]
   [canvas]
   [:h3 "select-color-pick"]
   [color-picker-component]
   [:h3 "my sand box"]
   [sand-box]])

;; -------------------------
;; Initialize app
(defn mount-root []
  (rdom/render
   [home]
   (.getElementById js/document "app")))

;; -------------------------
;; Translate routes -> page components


(defn init! []
  (comment clerk/initialize!)
  (comment accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (js/alert (str "router : " router
                       "\npath :" path
                       "\nmatch : " match))
        (reagent/after-render clerk/after-render!)
        (session/put! :route {:current-page current-page
                              :route-params route-params})
        (clerk/navigate-page! path)))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (comment (accountant/dispatch-current!))
  (mount-root))

(comment defonce gl-ctx (gl/gl-context "main"))
