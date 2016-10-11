(ns palr.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(def ^:const colors
  ["#EAEFBD" "#C9E3AC" "#90BE6D" "#EA9010" "#37371F"])

(defn Title []
  [:h1.center.white {:style {:font-size "300%"
                             :color "white"}} "Palr"])

(defn Container [& children]
  [:div.flex.justify-center {:style {:width "100vw" :height "100vh"}}
   (into
    [:div.flex.flex-column.relative {:style {:width "20rem" :padding-top "22.5vh"}}]
    children)])

(defn RouterButton
  ([url text] (RouterButton url text {}))
  ([url text attrs]
   [:button.btn.btn-primary.mt1.not-rounded
    (merge {:type "button" :on-click #(re-frame/dispatch [:change-route url])} attrs)
    text]))

(defn LeftArrow []
  [:div {:dangerouslySetInnerHTML {:__html "&larr;"}}])


(defn sync [atom attrs]
  (assoc attrs :value @atom :on-change #(reset! atom (-> % .-target .-value))))

(defn dispatch-submit [[type & atoms] & resetters]
  (fn [event]
    (re-frame/dispatch (into [type] (map deref atoms)))
    (doseq [atom atoms] (reset! atom ""))
    (doseq [atom resetters] (reset! atom ""))
    (.preventDefault event)))

(defn LoginPage []
  (let [username (reagent/atom "")
        password (reagent/atom "")]
    (fn []
      [:form.flex.flex-column {:on-submit (dispatch-submit [:login username password])}
       [:input.input.mb1 (sync username {:type "text" :placeholder "Username"})]
       [:input.input.mb1 (sync password {:type "password" :placeholder "Password"})]
       [:div.flex.relative
        [RouterButton "/" [LeftArrow] {:class "absolute" :style {:background-color (colors 1) :color (last colors)}}]
        [:button.btn.btn-primary.mt1.not-rounded.flex-auto {:type "submit" :style {:background-color (colors 2)}} "Sign In"]]])))

(defn LandingPage []
  [:div.flex.flex-column
   [RouterButton "/login" "Login" {:style {:background-color (colors 2)}}]
   [RouterButton "/register" "Register" {:style {:background-color (colors 2)}}]])

(defn RegisterPage []
  (let [email    (reagent/atom "")
        username (reagent/atom "")
        password (reagent/atom "")
        location (reagent/atom "")]
    (fn []
      [:form.flex.flex-column {:on-submit (dispatch-submit [:register username password] email location)}
       [:input.input.mb1 (sync username {:type "text" :placeholder "Username"})]
       [:input.input.mb1 (sync email {:type "email" :placeholder "Email"})]
       [:input.input.mb1 (sync password {:type "password" :placeholder "Password"})]
       [:input.input.mb1 (sync location {:type "text" :placeholder "Location"})]
       [:div.flex.relative
        [RouterButton "/" [LeftArrow] {:class "absolute" :style {:background-color (colors 1) :color (last colors)}}]
        [:button.btn.btn-primary.mt1.not-rounded.flex-auto {:type "submit" :style {:background-color (colors 2)}} "Sign Up"]]])))

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
