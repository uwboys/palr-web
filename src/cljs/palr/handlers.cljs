(ns palr.handlers
  (:require [re-frame.core :as re-frame]
            [palr.db :as db]
            [secretary.core :as secretary]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [day8.re-frame.async-flow-fx]
            [palr.middleware :as palr.mw]
            [palr.util]
            [palr.ws :as ws]
            [cljs.pprint]
            [js.alertify]))

(def common-interceptors [palr.middleware/persist-session!
                          re-frame/trim-v])

(defn reg-db [type func]
  (re-frame/reg-event-db
   type
   common-interceptors
   func))

(defn reg-fx [type func]
  (re-frame/reg-event-fx
   type
   common-interceptors
   func))

(reg-fx
 :init
 (fn [_ _]
   (let [signed-in? (-> db/default-db :session :access-token nil? not)]
     {:db db/default-db
      :dispatch-n (if signed-in? [[:connect-sio] [:fetch-user]] [])}))) ;; Redirect the user to the appropriate page

(reg-db
 :set-active-panel
 (fn [db [active-panel]]
   (assoc db :active-panel active-panel)))

(reg-fx
 :cond-sap
 (fn [{:keys [db]} [redirects dispatches panel]]
   {:dispatch-n
    (loop [redirects' (partition 2 redirects)]
      (if-let [[[func redirect] & others] redirects']
        (if (func db)
          [[:change-route redirect]]
          (recur others))
        (conj dispatches [:set-active-panel panel])))}))

(reg-db
 :change-route
 (fn [db [hash]]
   (secretary/dispatch! hash)
   (palr.util/set-hash! hash)
   db))

(defn auth-flow [first-dispatch success-action]
  {:first-dispatch first-dispatch
   :rules [{:when :seen? :events [success-action] :dispatch-n [[:connect-sio] [:fetch-user]]}
           {:when :seen? :events :fetch-user-success :dispatch [:auth-flow-success] :halt? true}]})

(reg-fx
 :login-flow
 (fn [_ [email password]]
   {:async-flow (auth-flow [:login email password] :login-success)}))

(reg-fx
 :register-flow
 (fn [_ [name email password location]]
   {:async-flow (auth-flow [:register name email password location] :register-success)}))

(reg-fx
 :auth-flow-success
 (fn [_ _]
   {:dispatch [:change-route "/pals"]}))

(reg-fx
 :request-pal-flow
 (fn [_ [type]]
   {:async-flow {:first-dispatch [:request-pal type]
                 :rules [{:when :seen? :events :request-pal-success :dispatch [:change-route "/conversations"]}]}}))

;; Fetch logged in user

(reg-fx
 :fetch-user
 (fn [{db :db} _]
   (let [access-token (-> db :session :access-token)]
     {:dispatch   [:set-progress    25]
      :http-xhrio {:method          :get
                   :uri             (palr.util/api "/users/me")
                   :timeout         8000
                   :headers         {:authorization access-token}
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:fetch-user-success]
                   :on-failure      [:fetch-user-failure]}})))

(reg-fx
 :fetch-user-success
 (fn [{:keys [db]} [user]]
   {:dispatch [:set-progress 100]
    :db (update db :session #(merge % user))}))

(reg-fx
 :fetch-user-failure
 (fn [_ event]
   (println "Failed to fetch user" event)
   (.error js/alertify "Failed to fetch user")
   {:dispatch [:set-progress 100]}))

;; Login

(reg-fx
 :login
 (fn [ctx [email password]]
   {:dispatch   [:set-progress    25]
    :http-xhrio {:method          :post
                 :uri             (palr.util/api "/login")
                 :timeout         8000
                 :params          {:email email :password password}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:login-success]
                 :on-failure      [:login-failure]}}))

(reg-fx
 :login-success
 (fn [{:keys [db]} [{:keys [accessToken]}]]
   {:dispatch [:set-progress 100]
    :db (assoc-in db [:session :access-token] accessToken)}))

(reg-fx
 :login-failure
 (fn [_ event]
   (println "Login failure" event)
   (.error js/alertify "Login failure")
   {:dispatch [:set-progress 100]}))

;; Register

(reg-fx
 :register
 (fn [ctx [name email password location]]
   {:dispatch   [:set-progress    25]
    :http-xhrio {:method          :post
                 :uri             (palr.util/api "/register")
                 :timeout         8000
                 :params          {:name name :email email :password password :location location}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:register-success]
                 :on-failure      [:register-failure]}}))

(reg-fx
 :register-success
 (fn [{:keys [db]} [{:keys [accessToken]}]]
   {:dispatch [:set-progress 100]
    :db (assoc-in db [:session :access-token] accessToken)}))

(reg-fx
 :register-failure
 (fn [_ [{:keys [response]}]]
   (.error js/alertify (:message response))
   {:dispatch [:set-progress 100]}))

;; Fetch Conversations

(reg-fx
 :fetch-conversations
 (fn [{db :db} _]
   (let [access-token (-> db :session :access-token)]
     {:dispatch   [:set-progress    25]
      :http-xhrio {:method          :get
                   :uri             (palr.util/api "/conversations")
                   :timeout         8000
                   :headers         {:authorization access-token}
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:fetch-conversations-success]
                   :on-failure      [:fetch-conversations-failure]}})))

(reg-fx
 :fetch-conversations-success
 (fn [{:keys [db]} [conversations]]
   {:dispatch [:set-progress 100]
    :db (assoc db :conversations conversations)}))

(reg-fx
 :fetch-conversations-failure
 (fn [_ event]
   (println "Failed to fetch conversations" event)
   (.error js/alertify "Failed to fetch conversations")
   {:dispatch [:set-progress 100]}))

;; Reset router params

(reg-db
 :reset-router-params
 (fn [db [params]]
   (assoc db :router-params params)))

;; request a pal

(reg-fx
 :request-pal
 (fn [{:keys [db]} [type]]
   (let [access-token (-> db :session :access-token)]
     {:dispatch   [:set-progress    25]
      :http-xhrio {:method          :post
                   :uri             (palr.util/api "/match")
                   :params          {:type type}
                   :timeout         8000
                   :headers         {:authorization access-token}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:request-pal-success]
                   :on-failure      [:request-pal-failure]}})))

(reg-fx
 :request-pal-success
 (fn [{:keys [db]} [response]]
   {:db (update db :session #(merge % response))
    :dispatch [:set-progress 100]}))

(reg-fx
 :request-pal-failure
 (fn [_ event]
   (println event)
   (.error js/alertify "Failed to request a pal")
   {:dispatch [:set-progress 100]}))


;; fetch messages

(reg-fx
 :fetch-messages
 (fn [{:keys [db]} [conversation-data-id]]
   (let [access-token (-> db :session :access-token)]
     {:dispatch   [:set-progress    25]
      :http-xhrio {:method          :get
                   :uri             (palr.util/api "/messages")
                   :params          {:conversationDataId conversation-data-id}
                   :timeout         8000
                   :headers         {:authorization access-token}
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:fetch-messages-success]
                   :on-failure      [:fetch-messages-failure]}})))

(reg-fx
 :fetch-messages-success
 (fn [{:keys [db]} [messages]]
   {:dispatch [:set-progress 100]
    :db (if (empty? messages)
          db
          (let [id (-> messages first :conversationDataId)
                ordered-messages (vec (sort-by #(-> % :createdAt js/Date. .getTime) messages))]
            (assoc-in db [:messages id] ordered-messages)))}))

(reg-fx
 :fetch-messages-failure
 (fn [_ event]
   (println event)
   (.error js/alertify "Failed to fetch messages")
   {:dispatch [:set-progress 100]}))

(reg-db
 :save-message
 (fn [db [message]]
   (update-in db [:messages (:conversationDataId message)] conj message)))

;; save a message

(reg-fx
 :send-message
 (fn [{:keys [db]} [conversation-data-id content]]
   (let [access-token (-> db :session :access-token)
         message {:conversationDataId conversation-data-id :content content}]
     {:dispatch-n [[:save-message   (assoc message :createdBy (:session db) :createdAt (-> (js/Date.) .toISOString))] [:set-progress    25]]
      :http-xhrio {:method          :post
                   :uri             (palr.util/api "/messages")
                   :params          message
                   :timeout         8000
                   :headers         {:authorization access-token}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:send-message-success]
                   :on-failure      [:send-message-failure]}})))

(reg-fx
 :send-message-success
 (fn [_ event]
   {:dispatch [:set-progress 100]}))

(reg-fx
 :send-message-failure
 (fn [_ event]
   (println event)
   {:dispatch [:set-progress 100]}))

;; progress bar

(reg-db
 :set-progress
 (fn [db [progress]]
   (println "Setting progress! " progress)
   (assoc db :progress progress)))

;; connect to socket io server

(reg-db
 :connect-sio
 (fn [db _]
   (ws/start-client! db)
   db))

;; Profile open

(reg-db
 :toggle-profile-open?
 (fn [db _]
   (update db :profile-open? not)))
