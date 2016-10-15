(ns palr.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [palr.components :refer [snow-canvas]]))

(def ^:const colors
  ["#EAEFBD" "#C9E3AC" "#90BE6D" "#EA9010" "#37371F"])

(defn Title []
  [:h1.center.white {:style {:font-size "300%"}} "Palr"])

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

(defn PalrBackground [url]
  [snow-canvas {:width (.-innerWidth js/window)
                :height (.-innerHeight js/window)
                :style {:background-image (str "url(" url ")")
                        :background-position "center center"
                        :background-size "cover"
                        :position "fixed"
                        :width "100vw"
                        :height "100vh"
                        :top 0
                        :z-index -1
                        :filter "blur(2px)"}}])



(defn Pals []
  (let [pals #{{:name "Ramanpreet", :permanent true, :avatar "http://placehold.it/50x50"}
               {:name "Maaz Ali", :permanent true, :avatar "http://placehold.it/50x50"}
               {:name "Mandish Shah", :permanent true, :avatar "http://placehold.it/50x50"}
               {:name "George", :permanent false, :avatar "http://placehold.it/50x50"}}]
    (fn []
      [:div.flex.items-center.flex-column {:style {:width "100vw" :height "100vh"}}
       [PalrBackground "https://images8.alphacoders.com/598/598776.jpg"]
       [:h1 {:style {:color (colors 4) :font-size "300%"}} "message pals"]
       [:ul.list-reset.mb4 {:style {:width "80%"}}
        (for [pal pals]
          ^{:key pal} [:li.px2.py1.mb2.flex.items-center.btn.regular.rounded
                       {:style {:background-color (colors 0)
                                :box-shadow (str "0 0.05rem 0.5rem" (colors 1))
                                :border-left (str "0.4rem solid " (if (:permanent pal) (colors 1) (colors 4)))}}
                       [:img.circle.mr2 {:src (:avatar pal)}]
                       [:span.h3 {:style {:color (colors 4)}} (:name pal)]])]])))

;; main

(derive ::landing ::front-page)
(derive ::login ::front-page)
(derive ::register ::front-page)

(defmulti panels identity)

(defmethod panels ::palr-me []
  [:div "Palr me!"])

(defmethod panels ::front-page [panel-key]
  [:div.flex.justify-center {:style {:width "100vw" :height "100vh"}}
   [PalrBackground "https://images8.alphacoders.com/598/598776.jpg"]
   [:div.flex.flex-column.relative {:style {:width "20rem" :padding-top "22.5vh"}}
    [Title]
    (condp = panel-key
      ::landing [LandingPage]
      ::login [LoginPage]
      ::register [RegisterPage])]])

(defmethod panels ::pals []
  [Pals])

(defmethod panels :default [] [:div])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [panels @active-panel])))
