(ns palr.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [palr.components :refer [snow-canvas]]
            [palr.util]))

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

(defn PalrButton
  ([child] (PalrButton {} child))
  ([attrs child]
   [:button.btn.btn-primary.mt1.not-rounded
    (palr.util/merge-attrs {:type "button" :style {:background-color (colors 2)}} attrs)
    child]))

(defn LoginPage []
  (let [email (reagent/atom "")
        password (reagent/atom "")]
    (fn []
      [:form.flex.flex-column {:on-submit (dispatch-submit [:login-flow email password])}
       [:input.input.mb1 (sync email {:type "text" :placeholder "Email"})]
       [:input.input.mb1 (sync password {:type "password" :placeholder "Password"})]
       [:div.flex.relative
        [RouterButton "/" [LeftArrow] {:class "absolute" :style {:background-color (colors 1) :color (last colors)}}]
        [PalrButton {:type "submit" :class "flex-auto"} "Sign In"]]])))

(defn LandingPage []
  [:div.flex.flex-column
   [RouterButton "/login" "Login" {:style {:background-color (colors 2)}}]
   [RouterButton "/register" "Register" {:style {:background-color (colors 2)}}]])

(defn RegisterPage []
  (let [name     (reagent/atom "")
        email    (reagent/atom "")
        password (reagent/atom "")
        location (reagent/atom "")]
    (fn []
      [:form.flex.flex-column {:on-submit (dispatch-submit [:register-flow name email password location])}
       [:input.input.mb1 (sync name {:type "text" :placeholder "Name"})]
       [:input.input.mb1 (sync email {:type "email" :placeholder "Email"})]
       [:input.input.mb1 (sync password {:type "password" :placeholder "Password"})]
       [:input.input.mb1 (sync location {:type "text" :placeholder "Location"})]
       [:div.flex.relative
        [RouterButton "/" [LeftArrow] {:class "absolute" :style {:background-color (colors 1) :color (last colors)}}]
        [PalrButton {:type "submit" :class "flex-auto"} "Sign Up"]]])))

(defn PalrBackground [url]
  [snow-canvas {:width  (.-innerWidth js/window)
                :height (.-innerHeight js/window)
                :style  {:background-image (str "url(" url ")")
                         :background-position "center center"
                         :background-size "cover"
                         :position "fixed"
                         :width "100vw"
                         :height "100vh"
                         :top 0
                         :z-index -1}}])

(defn Avatar [url]
  [:img.circle.my1.mx2 {:src url}])

(def avatar "http://placehold.it/40x40")

(defn ConversationCard [id name date]
  [:div.flex.btn.p0.regular.p-hover-bg-gray {:on-click #(re-frame/dispatch [:change-route (str "/conversations/" id)])}
   [Avatar avatar]
   [:div.py2.pr1.flex-auto.flex.flex-column.p-border-bottom
    [:div.flex.justify-between
     [:span.h4 {:style {:color (colors 4)}} name]
     [:span.gray date]]]])

(defn ConversationHeader [name]
  [:div.flex.bg-darken-1
   [Avatar avatar]
   [:div.py1.pr1.flex-auto.flex.flex-column.justify-center
    [:span.h3 {:style {:color (colors 4)}} name]]])

(defn Conversation [{:keys [id] {:keys [name]} :pal }]
  [ConversationHeader name])

(defn filter-one [func coll]
  (-> (filter func coll) first))

(defn PalrContainer [& attrs]
  [:div.flex.items-center.justify-center.flex-column {:style {:width "100vw" :height "100vh"}}
   [PalrBackground "../bg.png"]
   (into [:div.rounded {:style {:width "90%" :height "90%" :background-color "rgba(255, 255, 255, 0.95)"}}]
         attrs)])

(defn ConversationsPage [router-params]
  (let [conversations (re-frame/subscribe [:conversations])]
    (fn [router-params]
      [PalrContainer
       [:div.flex.clearfix
        [:div.col-4.m0.overflow-scroll
         [:h2.h2.center.px2 "Pals"]
         (for [{id :id {name :name} :pal} @conversations]
           ^{:key id} [ConversationCard id name])]
        [:div.col-8
         [:div.overflow-scroll.p2.bg-white {:style {:height "100%"}}
          (if-let [id (:id router-params)]
            [Conversation (filter-one #(= (:id %) id) @conversations)]
            [:div.flex.justify-center.items-center {:style {:height "100%"}} "Please pick a pal!"])]]]])))

(defn MatchMe []
  [PalrContainer
   [:div.flex.justify-center {:style {:width "100%" :height "100%"}}
    [:div.flex.flex-column.justify-center {:style {:width "20rem"}}
     [PalrButton {:on-click #(re-frame/dispatch [:request-pal "LEARN"])} "Learn"]
     [PalrButton {:on-click #(re-frame/dispatch [:request-pal "TALK"])} "Talk"]
     [PalrButton {:on-click #(re-frame/dispatch [:request-pal "LISTEN"])} "Listen"]]]])

;; main

(derive ::landing ::front-page)
(derive ::login ::front-page)
(derive ::register ::front-page)

(defmulti panels identity)

(defmethod panels ::match-me []
  [MatchMe])

(defmethod panels ::front-page [panel-key]
  [:div.flex.justify-center {:style {:width "100vw" :height "100vh"}}
   [PalrBackground "../bg.png"]
   [:div.flex.flex-column.relative {:style {:width "20rem" :padding-top "22.5vh"}}
    [Title]
    (condp = panel-key
      ::landing [LandingPage]
      ::login [LoginPage]
      ::register [RegisterPage])]])

(defmethod panels ::conversations [_ router-params]
  [ConversationsPage router-params])

(defmethod panels :default [] [:div])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])
        router-params (re-frame/subscribe [:router-params])]
    (fn []
      [panels @active-panel @router-params])))
