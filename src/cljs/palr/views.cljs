(ns palr.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [palr.components.common :refer [PalrBackground]]
            [palr.components.ui :refer [ScrollDiv Avatar Icon]]
            [palr.util]
            [cljs-time.format :as ctf]
            [cljs-time.core :as ctc]
            [js.react-progress-bar-plus]
            [clojure.string]))

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
   [:button.btn.btn-primary.not-rounded
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
        [PalrButton {:type "submit" :class "flex-auto mt1"} "Sign In"]]])))

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
        [PalrButton {:type "submit" :class "flex-auto mt1"} "Sign Up"]]])))

(defn ConversationCard [conversation-data-id name message]
  [:div.flex.btn.p0.regular.bg-white.p-hover-bg-gray.items-center {:on-click #(re-frame/dispatch [:change-route (str "/conversations/" conversation-data-id)])}
   [Avatar name]
   [:div.py2.pr1.flex-auto.flex.flex-column.p-border-bottom
    [:div.flex.justify-between
     [:span.h4.mb1 {:style {:color (colors 4)}} name]
     [:span.h5.gray (when (:createdAt message) (palr.util/get-date (:createdAt message)))]]
    [:div.h5.gray.truncate (:content message)]]])

(defn ConversationHeader [name]
  [:div.flex.bg-darken-1.p-no-shrink
   [Avatar name :sm]
   [:div.py1.pr1.flex-auto.flex.flex-column.justify-center
    [:span.h3 {:style {:color (colors 4)}} name]]])

(defn MessageBox [messages]
  [ScrollDiv {:class "flex-auto overflow-scroll mb1"}
   (for [message messages]
     ^{:key message} [:div
                      [:span.bold.mr1 (-> message :createdBy :name)]
                      (:content message)])])

(defn Conversation [convo messages]
  (let [content (reagent/atom "")]
    (fn [convo messages]
      [:form.flex.flex-column {:on-submit (dispatch-submit [:send-message (atom (:conversationDataId convo)) content])
                               :style {:height "100%"}}
       [ConversationHeader (-> convo :pal :name)]
       [:div.flex.flex-column.p1.flex-auto
        [MessageBox messages]
        [:div.flex.items-center.bg-darken-1.p1y.p-no-shrink
        [:input.flex-auto.input.m0.mr1 (sync content {:type "text" :rows 1 :placeholder "Write your letter"})]
         [PalrButton {:type "submit"} "Send"]]]])))

(defn filter-one [func coll]
  (-> (filter func coll) first))

(defn PalrContainer [& attrs]
  [:div.flex.items-center.justify-center.flex-column {:style {:width "100vw" :height "100vh"}}
   [PalrBackground "../bg.png"]
   (into [:div.rounded {:style {:width "90%" :height "90%" :background-color "rgba(255, 255, 255, 0.95)"}}]
         attrs)])

(defn CircularButton [])

(defn ConversationSidebar [profile-open? session conversations messages]
  [:div.col-4.m0.overflow-scroll
   [:div.bg-darken-1.flex.items-center.justify-between
    [Avatar (:name session) :sm]
    [:button.btn.circle.p-focus-no-border.center.p-focus-no-shadow.p-transition-1.gray.p0 {:type "button"
                                                                                 :class (if profile-open? "bg-darken-2" "")
                                                                                 :on-click #(re-frame/dispatch [:toggle-profile-open?])
                                                                                 :style {:width "30px" :height "30px" :line-height "30px"}}
     [Icon {:class "user" :size 2}]]]
   (if profile-open?
     "Profile page!"
     (for [{conversation-data-id :conversationDataId date :lastMessageDate {name :name} :pal} conversations]
       ^{:key conversation-data-id} [ConversationCard conversation-data-id name (last (get messages conversation-data-id []))]))])

(defn ConversationsPage [router-params]
  (let [profile-open? (re-frame/subscribe [:profile-open?])
        session (re-frame/subscribe [:session])
        conversations (re-frame/subscribe [:conversations])
        messages (re-frame/subscribe [:messages])]
    (fn [router-params]
      [PalrContainer
       [:div.flex.clearfix {:style {:height "100%"}}
        [ConversationSidebar @profile-open? @session @conversations @messages]
        [:div.col-8
         [:div.overflow-scroll.bg-white {:style {:height "100%"}}
          (if-let [id (:id router-params)]
            (let [convo (filter-one #(= (:conversationDataId %) id) @conversations)
                  convo-messages (get @messages (:conversationDataId convo) [])]
              [Conversation convo convo-messages])
            [:div.flex.justify-center.items-center {:style {:height "100%"}} "Please pick a pal!"])]]]])))

(defn MatchMe []
  [PalrContainer
   [:div.flex.justify-center {:style {:width "100%" :height "100%"}}
    [:div.flex.flex-column.justify-center {:style {:width "20rem"}}
     [PalrButton {:class "mb1" :on-click #(re-frame/dispatch [:request-pal-flow "LEARN"])} "Learn"]
     [PalrButton {:class "mb1" :on-click #(re-frame/dispatch [:request-pal-flow "TALK"])} "Talk"]
     [PalrButton {:on-click #(re-frame/dispatch [:request-pal-flow "LISTEN"])} "Listen"]]]])

(defn progress-bar []
  (fn [props]
    (.createElement js/React js/ReactProgressBarPlus props)))

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
        router-params (re-frame/subscribe [:router-params])
        progress (re-frame/subscribe [:progress])]
    (fn []
      [:div
       [progress-bar #js {:percent @progress}]
       [panels @active-panel @router-params]])))
