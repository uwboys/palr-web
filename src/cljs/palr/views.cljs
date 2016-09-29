(ns palr.views
    (:require [re-frame.core :as re-frame]))

(defn LandingPage []
  [:div {:style {:width "100vw"
                 :height "100vh"
                 :display "flex"
                 :justify-content "center"
                 :align-items "center"}}
   [:div {:style {:display "flex" :flex-direction "column"}}
    [:button {:type "button"} "Login"]
    [:button {:type "button"} "Register"]]])

;; about

(defn about-panel []
  (fn []
    [:div "This horse the About Page."
     [:div [:a {:href "#/"} "go to Home Page"]]]))


;; main

(defmulti panels identity)
(defmethod panels :landing-page [] [LandingPage])
(defmethod panels :about-panel [] [about-panel])
(defmethod panels :default [] [:div])

(defn show-panel
  [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [show-panel @active-panel])))
