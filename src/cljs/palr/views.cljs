(ns palr.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(def ^:const sakura-bg "https://66.media.tumblr.com/41281654247d0c1b622467039c2cdb05/tumblr_ncmmky30z11rxgopvo1_500.gif")
(def ^:const snow-bg "http://i.makeagif.com/media/10-12-2015/rb3ZJZ.gif")

(def ^:const bg-image (str "url(" sakura-bg ")"))

(defn Title []
  [:h1.center.white {:style {:font-size "300%"
                             :color "white"}} "palr"])

(defn BlurredBackground []
  [:div.fixed.top-0.left-0.bottom-0.right-0 {:style {;:background-image bg-image
                                                     :background-size "cover"
                                                     :z-index -1
                                                     :filter "blur(10px)"}}])

(defn Container [& children]
  [:div.flex.justify-center {:style {:width "100vw" :height "100vh"}}
   (into
    [:div.flex.flex-column.relative {:style {:width "20rem" :padding-top "20vh"}}]
    children)])

(defn RouterButton [url text]
  [:button {:type "button"
            :on-click #(re-frame/dispatch [:change-route url])
            :class "btn btn-primary mt1"} text])

(defn LeftArrow []
  [:div {:dangerouslySetInnerHTML {:__html "&larr;"}}])


(defn sync [atom attrs]
  (assoc attrs :value @atom :on-change #(reset! atom (-> % .-target .-value))))

(defn LoginPage []
  (let [username (reagent/atom "")
        password (reagent/atom "")]
    (fn []
      [:form.flex.flex-column {:on-submit #(do
                                             (re-frame/dispatch [:login @username @password])
                                             (reset! username "")
                                             (reset! password "")
                                             (.preventDefault %))}
       [:input.input.mb1 (sync username {:type "text" :placeholder "Username"})]
       [:input.input.mb1 (sync password {:type "password" :placeholder "Password"})]
       [:button.btn.btn-primary.mt1 {:type "submit"} "Sign In"]
       [RouterButton "/" [LeftArrow]]])))

(defn LandingPage []
  [:div.flex.flex-column
   [RouterButton "/login" "Login"]
   [RouterButton "/register" "Register"]])

(defn RegisterPage []
  [:div.flex.flex-column 
   [:input.input.mb1 {:type "text" :placeholder "Username"}]
   [:input.input.mb1 {:type "email" :placeholder "Email"}]
   [:input.input.mb1 {:type "password" :placeholder "Password"}]
   [:input.input.mb1 {:type "text" :placeholder "Location"}]
   [RouterButton "/register" "Sign Up"]
   [RouterButton "/" [LeftArrow]]])

;; main

(derive ::landing ::front-page)
(derive ::login ::front-page)
(derive ::register ::front-page)

(defmulti panels identity)
(defmethod panels ::front-page [panel-key]
  [Container
   [Title]
   (condp = panel-key
     ::landing [LandingPage]
     ::login [LoginPage]
     ::register [RegisterPage])])
(defmethod panels :default [] [:div])

(defn show-panel
  [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [show-panel @active-panel])))
