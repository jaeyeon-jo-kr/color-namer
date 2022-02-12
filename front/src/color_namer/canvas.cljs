(ns color-namer.canvas
(:require
 [color-namer.app :as app]
 [reagent.core :as reagent :refer [atom cursor]]
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

(defn get-activated-color []
  (get-in @app/state [:canvas :activated :color]))

(defn set-activated-color! [color]
  (js/alert color)
  (when 
   (swap! assoc-in app/state [:canvas :activated :color] color)))

(defn combine-model-shader-and-camera
  [context model shader-spec camera]
  (-> model
      (gl/as-gl-buffer-spec {})
      (assoc :shader (shaders/make-shader-from-spec context shader-spec))
      (gl/make-buffers-in-spec context glc/static-draw)
      (cam/apply camera)))

(defn draw
  [context model shader-spec camera]
   (when-let [color (.-fillStyle context)]
     (set-activated-color! color))
  (comment .fillRect context 0 0 150 150)
  (doto context
    (gl/clear-color-and-depth-buffer 0 0 0 1 1)
    (gl/draw-with-shader (combine-model-shader-and-camera context model shader-spec camera))))

(defn canvas []
  (let
   [component-status
    (atom
     {:status {}
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
                      (draw triangle shader-spec camera))))}]]))))