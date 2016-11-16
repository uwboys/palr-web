(ns palr.handlers
  (:require [re-frame.core :as re-frame]
            [palr.db :as db]
            [secretary.core :as secretary]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [day8.re-frame.async-flow-fx]
            [palr.middleware :as palr.mw]
            [palr.util]))

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

(reg-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(reg-db
 :set-active-panel
 (fn [db [active-panel]]
   (assoc db :active-panel active-panel)))

(reg-fx
 :cond-sap
 (fn [{:keys [db]} [redirects panel]]
   {:dispatch
    (loop [redirects' (partition 2 redirects)]
      (if-let [[[func redirect] & others] redirects']
        (if (func db)
          [:change-route redirect]
          (recur others))
        [:set-active-panel panel]))}))

(reg-db
 :change-route
 (fn [db [hash]]
   (secretary/dispatch! hash)
   (palr.util/set-hash! hash)
   db))

(defn auth-flow [first-dispatch success-action]
  {:first-dispatch first-dispatch
   :rules [{:when :seen? :events [success-action] :dispatch [:fetch-conversations]}
           {:when :seen? :events [:fetch-conversations-success] :dispatch [:auth-flow-success] :halt? true}]})

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
 (fn [_ event]
   (println "Register failure" event)
   (.alert js/window "Registration failure")
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
   {:dispatch [:set-progress 100]}))

;;

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
 (fn [_ event]
   (println event)
   {:dispatch [:set-progress 100]}))

(reg-fx
 :request-pal-failure
 (fn [_ event]
   (println event)
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
          (let [id (-> messages first :conversationDataId)]
            (assoc-in db [:messages id] messages)))}))

(reg-fx
 :fetch-messages-failure
 (fn [_ event]
   (println event)
   {:dispatch [:set-progress 100]}))

;; save message flow

(reg-fx
 :save-message-flow
 (fn [cfx [conversation-data-id content]]
   {:async-flow {:first-dispatch [:save-message conversation-data-id content]
                 :rules [{:when :seen? :events [:save-message-success] :dispatch [:fetch-messages conversation-data-id]}]}}))

;; save a message

(reg-fx
 :save-message
 (fn [{:keys [db]} [conversation-data-id content]]
   (let [access-token (-> db :session :access-token)]
     (println conversation-data-id content)
     {:dispatch   [:set-progress    25]
      :http-xhrio {:method          :post
                   :uri             (palr.util/api "/messages")
                   :params          {:conversationDataId conversation-data-id :content content}
                   :timeout         8000
                   :headers         {:authorization access-token}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:save-message-success]
                   :on-failure      [:save-message-failure]}})))

(reg-fx
 :save-message-success
 (fn [_ event]
   (println event)
   {:dispatch [:set-progress 100]}))

(reg-fx
 :save-message-failure
 (fn [_ event]
   (println event)
   {:dispatch [:set-progress 100]}))

;; progress bar

(reg-db
 :set-progress
 (fn [db [progress]]
   (println "Setting progress! " progress)
   (assoc db :progress progress)))
