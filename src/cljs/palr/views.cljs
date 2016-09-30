(ns palr.views
    (:require [re-frame.core :as re-frame]))

(defn FullScreenXYCenter [child]
  [:div.flex.items-center.justify-center {:style {:width "100vw" :height "100vh"}}
   child])

(defn RouterButton [url text]
  [:button {:type "button"
            :on-click #(re-frame/dispatch [:change-route url])
            :class "btn btn-primary mt1"} text])

(defn LeftArrow []
  [:div {:dangerouslySetInnerHTML {:__html "&larr;"}}])

(defn LoginPage []
  [FullScreenXYCenter
   [:div.flex.flex-column {:style {:padding-bottom "15rem" :width "20rem"}}
    [:input.input.mb1 {:type "email" :placeholder "Username"}]
    [:input.input.mb1 {:type "password" :placeholder "Password"}]
    [RouterButton "/login" "Sign In"]
    [RouterButton "/" [LeftArrow]]]])

(defn LandingPage []
  [FullScreenXYCenter
   [:div.flex.flex-column {:style {:padding-bottom "15rem" :width "20rem"}}
    [:h1.center {:style {:font-size "300%"}} "Palr"]
    [RouterButton "/login" "Login"]
    [RouterButton "/register" "Register"]]])

(defn RegisterPage []
  [FullScreenXYCenter
   [:div.flex.flex-column {:style {:padding-bottom "15rem" :width "20rem"}}
    [:input.input.mb1 {:type "text" :placeholder "Username"}]
    [:input.input.mb1 {:type "email" :placeholder "Email"}]
    [:input.input.mb1 {:type "password" :placeholder "Password"}]
    [:input.input.mb1 {:type "text" :placeholder "Location"}]
    [RouterButton "/register" "Sign up"]
    [RouterButton "/" [LeftArrow]]]])

;; main

(defmulti panels identity)
(defmethod panels :landing [] [LandingPage])
(defmethod panels :login [] [LoginPage])
(defmethod panels :register [] [RegisterPage])
(defmethod panels :default [] [:div])

(defn show-panel
  [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [show-panel @active-panel])))
