(ns palr.components.ui
  (:require [reagent.core :as reagent]
            [palr.util :as util]))

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
