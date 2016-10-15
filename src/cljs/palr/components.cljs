(ns palr.components
  (:require [reagent.core :as reagent]))

(defn random!
  ([] (.random js/Math))
  ([n] (* (random!) n))
  ([a b] (+ (random! (- b a)) a)))

(defn create-particles! [[W H] n]
  (for [_ (range n)]
    {:x (random! W)
     :y (random! H)
     :r (random! 1 5)
     :d (random! n)}))

(defn sin [x]
  (.sin js/Math x))

(defn cos [x]
  (.cos js/Math x))

(defn draw! [particles [W H] ctx]
  (.clearRect ctx 0 0 W H)
  (aset ctx "fillStyle" "rgba(255, 255, 255, 0.8)")
  (.beginPath ctx)
  (doseq [p particles]
    (let [{:keys [x y r]} p]
      (.moveTo ctx x y)
      (.arc ctx x y r 0 (* 2 (.-PI js/Math) 2) true)))
  (.fill ctx))

(defn update! [particles [W H] angle]
  (map-indexed
   (fn [i p]
     (let [dy (+ (cos (+ angle (:d p))) 1 (/ (:r p) 2))
           dx (* 2 (sin angle))
           x (+ dx (:x p))
           y (+ dy (:y p))]
       (if (or (< x -5) (< (+ 5 W) x) (< H y))
         (if (pos? (mod i 3))
           (assoc p :x (random! W) :y -10)
           (if (pos? (sin angle))
             (assoc p :x -5 :y (random! H))
             (assoc p :x (+ 5 W) :y (random! H))
             ))
         (assoc p :x x :y y))))
   particles))

(defn animate! [canvas]
  (let [dims [(.-width canvas) (.-height canvas)]
        particles (atom (create-particles! dims 25))
        angle (atom 0)]
    ((fn tick []
       (js/requestAnimationFrame tick)
       (let [dims [(.-width canvas) (.-height canvas)]
             context (.getContext canvas "2d")]
         (draw! @particles dims context)
         (swap! angle + 0.01)
         (swap! particles update! dims @angle))))))

(defn snow-canvas [width height]
  (reagent/create-class
   {:component-did-mount
    (fn [this]
      (let [canvas (reagent/dom-node this)]
        (animate! canvas)))

    :reagent-render
    (fn [& attrs]
      (into [:canvas] attrs))}))
