(ns palr.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [palr.components.common :refer [PalrBackground]]
            [palr.components.ui :refer [ScrollDiv Avatar Icon Select Creatable Textarea EllipsisDropdown Dropzone]]
            [palr.util]
            [cljs-time.format :as ctf]
            [cljs-time.core :as ctc]
            [js.react-progress-bar-plus]
            [clojure.string]
            [cljs.pprint]
            [palr.constant :as pc]))

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

(defn sync-Select [atom attrs]
  (assoc attrs :value @atom :onChange #(reset! atom %)))

(defn sync-Textarea [atom attrs]
  (assoc attrs :value @atom :onChange #(reset! atom (-> % .-target .-value))))

(defn prevent-default [event]
  (.preventDefault event)
  event)

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
        country (reagent/atom "")]
    (fn []
      [:form.flex.flex-column {:on-submit (dispatch-submit [:register-flow name email password country])}
       [:input.input.mb1 (sync name {:type "text" :placeholder "Name"})]
       [:input.input.mb1 (sync email {:type "email" :placeholder "Email"})]
       [:input.input.mb1 (sync password {:type "password" :placeholder "Password"})]
       [Select (sync-Select country {:options pc/Countries :placeholder "Country" :clearable false :class "border-none" :simpleValue true})]
       [:div.flex.relative
        [RouterButton "/" [LeftArrow] {:class "absolute" :style {:background-color (colors 1) :color (last colors)}}]
        [PalrButton {:type "submit" :class "flex-auto mt1"} "Sign Up"]]])))

(defn ConversationCard [conversation message]
  (let [name (-> conversation :pal :name)
        conversation-data-id (-> conversation :conversationDataId)
        permanent? (-> conversation :isPermanent)]
    [:div.flex.btn.p0.regular.p-hover-bg-gray.bg-white.items-center {:on-click #(re-frame/dispatch [:change-route (str "/conversations/" conversation-data-id)])}
     [Avatar {:name name :bg-color (if permanent? "p-bg-green" "bg-gray") :size :sm}]
     [:div.py2.pr1.flex-auto.flex.flex-column.p-border-bottom
      [:div.flex.justify-between
       [:span.h4.mb1 {:style {:color (colors 4)}} name]
       [:span.h5.gray (when (:createdAt message) (palr.util/get-date (:createdAt message)))]]
      [:div.h5.gray.truncate.mr3 (:content message)]]]))

(defn ConversationHeader [{:keys [isPermanent id requestPermanent] {:keys [name]} :pal :as conversation}]
  [:div.flex.bg-darken-1.p-no-shrink.justify-between.items-center.px1
   [:div.flex
    [Avatar {:name name :size :sm :on-click #(re-frame/dispatch [:toggle-pal-profile-open?])}]
    [:div.py1.pr1.flex-auto.flex.items-center
     [:a.h3 {:on-click #(re-frame/dispatch [:toggle-pal-profile-open?]) :style {:cursor "pointer" :color (colors 4)}} name]]]
   (when-not isPermanent
     [PalrButton {:on-click #(re-frame/dispatch [:request-permanence id]) :disabled requestPermanent} "Request Permanence"])])


(defn MessageBox [mine? messages]
  [ScrollDiv {:class "flex-auto overflow-scroll mb1 px1 flex flex-column"}
   (for [message messages]
     ^{:key message} [:div.p1.m1.rounded {:class (if (mine? message) "self-start p-bg-green" "self-end bg-darken-1")
                                          :style {:white-space "pre-line"}}
                      (:content message)])])

(defn Conversation [convo messages]
  (let [content (reagent/atom "")]
    (fn [convo messages]
      [:form.flex.flex-column {:on-submit (dispatch-submit [:send-message (atom (:conversationDataId convo)) content])
                               :style {:height "100%"}}
       [ConversationHeader convo]
       [:div.flex.flex-column.p1.flex-auto
        [MessageBox #(= (-> % :createdBy :id) (-> convo :user :id)) messages]
        [:div.flex.items-end.bg-darken-1.p1.p-no-shrink.rounded
         [Textarea (sync-Textarea content {:type "text" :className "flex-auto textarea p-resize-none m0 mr1" :minRows 1 :maxRows 20 :placeholder "Write your letter"})]
         [PalrButton {:type "submit"} "Send"]]]])))

(defn filter-one [func coll]
  (-> (filter func coll) first))

(defn PalrContainer [& attrs]
  [:div.flex.items-center.justify-center.flex-column {:style {:width "100vw" :height "100vh"}}
   [PalrBackground "../bg.png"]
   (into [:div.rounded {:style {:width "90%" :height "90%" :background-color "rgba(255, 255, 255, 0.95)"}}]
         attrs)])

(defn ProfileInputBox [label input]
  [:div.rounded.mb2.bg-white.p1
   [:label label input]])

(defn ProfileImage [{:keys [src size] :or {size "150px"}}]
  [:div.flex.justify-center.py2
   [:img.circle.fit {:src src :style {:width size :height size}}]])

(defn lowercase [str]
  (.toLowerCase (or str "")))

(defn ProfileSidebar [session]
  (let [imageUrl (reagent/atom (:imageUrl session))
        email (reagent/atom (:email session))
        password (reagent/atom "")
        gender (reagent/atom (:gender session))
        country (reagent/atom (lowercase (:country session)))
        age (reagent/atom (:age session))
        ethnicity (reagent/atom (lowercase (:ethnicity session)))
        hobbies (reagent/atom (clj->js (map #(identity {:label % :value %}) (:hobbies session)))) ;; multi-select is strange. We need to make the selected value conform the structure of the option elements
        dispatch-update #(re-frame/dispatch [:save-profile @email @password @gender @country @age @ethnicity (.map @hobbies (fn [hobby] (.-value hobby)))])
        handle-submit (comp dispatch-update prevent-default)]
    (fn [session]
      [:form.p2.flex.flex-column.bg-darken-1.p-h-100 {:on-submit handle-submit}
       ;[Dropzone {:on-drop #(println "test")}
        [ProfileImage {:src @imageUrl :size "100px"}]
        ;]
       [:div.px2
        [:div
         [ProfileInputBox
          "E-Mail"
          [:input.input.mb0 (sync email {:type "email"})]]
         [ProfileInputBox
          "Password"
          [:input.input.mb0 (sync password {:type "password"})]]
         [ProfileInputBox
          "Gender"
          [Select (sync-Select gender {:options pc/Genders :clearable false :class "border-none" :simpleValue true})]]
         [ProfileInputBox
          "Country"
          [Select (sync-Select country {:options pc/Countries :clearable false :class "border-none" :simpleValue true})]]
         [ProfileInputBox
          "Age"
          [Select (sync-Select age {:options pc/Ages :clearable false :class "border-none" :simpleValue true})]]
         [ProfileInputBox
          "Ethnicity"
          [Creatable (sync-Select ethnicity {:options pc/Ethnicities :clearable false :class "border-none" :simpleValue true})]]
         [ProfileInputBox
          "Hobbies"
          [Creatable (sync-Select hobbies {:multi true})]]]
        [PalrButton {:class "p-w-100" :type "submit"} "Update"]]])))

(defn InfoBubble [title description]
  [:div.bg-white.rounded.p1.m1
   [:div.gray.h5.pt0.pb1.px1 title]
   [:hr.m0]
   [:div.h4.p1 (or description "...")]])


(defn PalProfileSidebar [pal]
  (let [{:keys [name country age ethnicity gender email imageUrl]} pal]
    [:div.p2.bg-darken-1.p-h-100
     [:button.btn.border-none.p-focus-no-shadow.py0.px1.gray {:on-click #(re-frame/dispatch [:toggle-pal-profile-open?])}
      [Icon {:class "times"}]]
     [:div.h1.center.mb2 (str name "'s Profile")]
     [ProfileImage {:src imageUrl}]
     [:div.px2
      [InfoBubble "E-Mail" email]
      [InfoBubble "Country" country]
      [InfoBubble "Age" age]
      [InfoBubble "Ethnicity" ethnicity]
      [InfoBubble "Gender" gender]]]))


(defn ConversationSidebar [profile-open? session conversations messages]
  [:div.m0.p-h-100
   [:div.bg-darken-1.flex.items-center.justify-between.pr1
    [Avatar {:name (:name session) :size :sm}]
    [:div.flex
     [:button.btn.circle.center.p-focus-no-shadow.p-transition-1.gray.p0.border-none.px1 {:type "button"
                                                                                          :class (if profile-open? "bg-darken-2" "")
                                                                                          :on-click #(re-frame/dispatch [:toggle-profile-open?])
                                                                                          :style {:width "30px" :height "30px" :line-height "30px"}}
      [Icon {:class "user" :size 2}]]
     [EllipsisDropdown {:logout "Logout"} #(case % :logout (re-frame/dispatch [:logout]))]]]
   [:div.overflow-scroll.p-h-100
    (if profile-open?
      [ProfileSidebar session]
      (for [conversation conversations]
        ^{:key conversation} [ConversationCard conversation (last (get messages (:conversationDataId conversation) []))]))]])

(defn ConversationsPage [router-params]
  (let [profile-open? (re-frame/subscribe [:profile-open?])
        pal-profile-open? (re-frame/subscribe [:pal-profile-open?])
        session (re-frame/subscribe [:session])
        conversations (re-frame/subscribe [:conversations])
        messages (re-frame/subscribe [:messages])]
    (fn [router-params]
      [PalrContainer
       [:div.flex.clearfix.p-h-100
        [:div.col.col-4.lg-col.lg-col-3 [ConversationSidebar @profile-open? @session @conversations @messages]]
        (if-let [id (:id router-params)]
          (let [convo (filter-one #(= (:conversationDataId %) id) @conversations)
                convo-messages (get @messages (:conversationDataId convo) [])]
            [:div.col.col-8.lg-col.lg-col-9.flex.clearfix.p-h-100
             [:div.p-h-100 {:class (if @pal-profile-open? "col col-6 lg-col lg-col-8" "col col-12")}
              [:div.overflow-scroll.bg-white.p-h-100
               [Conversation convo convo-messages]]]
             [:div.flex-auto
              (when @pal-profile-open?
                [PalProfileSidebar (:pal convo)])]])
          [:div.col.col-8.lg-col.lg-col-9.p-h-100.flex.items-center.justify-center
           [:div "Please pick a pal!"]])]])))

(defn MatchMe []
  (let [session (re-frame/subscribe [:session])]
    (fn []
      [PalrContainer
       [:div.flex.clearfix {:style {:width "100%" :height "100%"}}
        [:div.col.col-4
         [:div.flex.flex-end.bg-darken-1.p-flex-row-reverse.py1
          [EllipsisDropdown {:logout "Logout"} #(case % :logout (re-frame/dispatch [:logout]))]]
         [ProfileSidebar @session]]
        [:div.col.col-8.flex.flex-column.justify-center.items-center
         [:div.flex.flex-column {:style {:width "20rem"}}
          [PalrButton {:class "mb1" :on-click #(re-frame/dispatch [:request-pal-flow "LEARN"])} "Learn"]
          [PalrButton {:class "mb1" :on-click #(re-frame/dispatch [:request-pal-flow "TALK"])} "Talk"]
          [PalrButton {:on-click #(re-frame/dispatch [:request-pal-flow "LISTEN"])} "Listen"]]]]])))

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
