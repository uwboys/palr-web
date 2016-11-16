(ns palr.components.common
  (:require [reagent.core :as reagent]
            [palr.components.ui :refer [SnowCanvas]]))

(defonce width (reagent/atom (.-innerWidth js/window)))
(defonce height (reagent/atom (.-innerHeight js/window)))

(defn- reset-dimensions [event]
  (let [w (.-innerWidth js/window)
        h (.-innerHeight js/window)]
    (reset! width w)
    (reset! height h)))

(defonce init
  (.addEventListener js/window "resize" reset-dimensions))

(defn PalrBackground [url]
  [SnowCanvas {:width  @width
               :height @height
               :style  {:background-image (str "url(" url ")")
                        :background-position "center center"
                        :background-size "cover"
                        :position "fixed"
                        :width "100vw"
                        :height "100vh"
                        :top 0
                        :z-index -1}}])
