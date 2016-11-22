(ns palr.components.ui
  (:require [reagent.core :as reagent]
            [palr.util :as util]
            [js.classnames]
            [js.react-input-autosize]
            [js.react-select]
            [js.react-textarea-autosize]))

;; SnowCanvas

(defn- create-particles! [[W H] n]
  (for [_ (range n)]
    {:x (util/random! W)
     :y (util/random! H)
     :r (util/random! 1 5)
     :d (util/random! n)}))

(defn- sin [x]
  (.sin js/Math x))

(defn- cos [x]
  (.cos js/Math x))

(defn- draw! [particles [W H] ctx]
  (.clearRect ctx 0 0 W H)
  (aset ctx "fillStyle" "rgba(255, 255, 255, 0.8)")
  (.beginPath ctx)
  (doseq [p particles]
    (let [{:keys [x y r]} p]
      (.moveTo ctx x y)
      (.arc ctx x y r 0 (* 2 (.-PI js/Math) 2) true)))
  (.fill ctx))

(defn- update! [particles [W H] angle]
  (map-indexed
   (fn [i p]
     (let [dy (+ (cos (+ angle (:d p))) 1 (/ (:r p) 2))
           dx (* 2 (sin angle))
           x (+ dx (:x p))
           y (+ dy (:y p))]
       (if (or (< x -5) (< (+ 5 W) x) (< H y))
         (if (pos? (mod i 3))
           (assoc p :x (util/random! W) :y -10)
           (if (pos? (sin angle))
             (assoc p :x -5 :y (util/random! H))
             (assoc p :x (+ 5 W) :y (util/random! H))
             ))
         (assoc p :x x :y y))))
   particles))

(defn- animate! [stop canvas]
  (let [dims [(.-width canvas) (.-height canvas)]
        particles (atom (create-particles! dims 30))
        angle (atom 0)]
    ((fn tick []
       (when-not @stop (js/requestAnimationFrame tick))
       (let [dims [(.-width canvas) (.-height canvas)]
             context (.getContext canvas "2d")]
         (draw! @particles dims context)
         (swap! angle + 0.01)
         (swap! particles update! dims @angle))))))

(defn SnowCanvas [width height]
  (let [stop (atom false)]
    (reagent/create-class
     {:component-did-mount
      (fn [this]
        (let [canvas (reagent/dom-node this)]
          (animate! stop canvas)))

      :component-will-unmount
      (fn [this]
        (reset! stop true))

      :reagent-render
      (fn [& attrs]
        (into [:canvas] attrs))})))

;; ScrollDiv

(defn ScrollDiv [& attrs]
  (let [node                 (reagent/atom nil)
        should-scroll-bottom (atom false)]
    (reagent/create-class
     {:component-did-mount
      (fn [this]
        (reset! node (reagent/dom-node this)))

      :component-will-update
      (fn [this]
        (when-let [el @node]
          (reset! should-scroll-bottom (= (+ (.-scrollTop el) (.-offsetHeight el)) (.-scrollHeight el)))))

      :component-did-update
      (fn [this]
        (when-let [el @node]
          (when @should-scroll-bottom
            (aset el "scrollTop" (.-scrollHeight el)))))

      :reagent-render
      (fn [& attrs]
        (into [:div] attrs))})))

(defn Avatar [{:keys [name size bg-color on-click] :or {size :md bg-color "bg-gray"}}]
  (let [size ({:md "50px" :sm "40px" :xs "30px" :lg "60px"} size)
        styles {:width size :height size :line-height size}]
    [:div.py1.px2
     [:div.circle.center.h1.white.p-no-select {:style (if (-> on-click nil? not) (merge styles {:cursor "pointer"}) styles)
                                               :class bg-color
                                               :on-click on-click}
      (.toUpperCase (or (first name) ""))]]))

;; Font-Awesome

(defn Icon [{:keys [name class size rotate flip fixed-width spin pulse stack inverse component]
             :or {component :i}
             :as props}]
  [component
   (assoc props
          :class (str "fa fa-" class " "
                      (if size (str "fa-" size)) " "
                      (if rotate (str "fa-rotate-" rotate)) " "
                      (if flip (str "fa-flip-" flip)) " "
                      (if fixed-width "fa-fw") " "
                      (if spin "fa-spin") " "
                      (if pulse "fa-pulse") " "
                      (if stack (str "fa-stack-" stack)) " "
                      (if inverse "fa-inverse")))])

(defn Select [props]
  (fn [props]
    (.createElement js/React js/Select (clj->js props))))

(defn Creatable [props]
  (fn [props]
    (.createElement js/React (.-Creatable js/Select) (clj->js props))))

(defn Textarea[props]
  (fn [props]
    (.createElement js/React js/TextareaAutosize (clj->js props))))


;; Ellipsis dropdown

(defn EllipsisDropdown [items on-click]
  (let [open (reagent/atom false)]
    (fn [items on-click]
      [:div.relative.px1.p-ellipsis-dropdown
       [:button.btn.circle.center.p-focus-no-shadow.p-transition-1.gray.p0.border-none {:style {:width "30px" :height "30px" :line-height "30px"}}
        [Icon {:class "ellipsis-v" :size 3}]]
       [:div.absolute.bg-white.p-ellipsis-dropdown-items.rounded.p-hover-bg-gray.z1 {:style {:top "100%" :right "0" :box-shadow "0px 0px 0.05rem 0.05rem lightgray"}}
        (for [[key label] (seq items)]
          ^{:key key} [:div.btn.p-focus-no-shadow.border-none {:on-click #(on-click key)} label])]])))
