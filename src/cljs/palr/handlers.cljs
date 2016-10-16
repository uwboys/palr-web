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

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :set-active-panel
 common-interceptors
 (fn [db [active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-fx
 :cond-sap
 common-interceptors
 (fn [{:keys [db]} [redirects panel]]
   {:dispatch
    (loop [redirects' (partition 2 redirects)]
      (if-let [[[func redirect] & others] redirects']
        (if (func db)
          [:change-route redirect]
          (recur others))
        [:set-active-panel panel]))}))

(re-frame/reg-event-db
 :change-route
 common-interceptors
 (fn [db [hash]]
   (do
     (secretary/dispatch! hash)
     (palr.util/set-hash! hash)
     db)))

(defn auth-flow [first-dispatch success-action]
  {:first-dispatch first-dispatch
   :rules [{:when :seen? :events [success-action] :dispatch [:fetch-conversations]}
           {:when :seen? :events [:fetch-conversations-success] :dispatch [:auth-flow-success] :halt? true}]})

(re-frame/reg-event-fx
 :login-flow
 common-interceptors
 (fn [_ [email password]]
   {:async-flow (auth-flow [:login email password] :login-success)}))

(re-frame/reg-event-fx
 :register-flow
 common-interceptors
 (fn [_ [name email password location]]
   {:async-flow (auth-flow [:register name email password location] :register-success)}))

(re-frame/reg-event-fx
 :auth-flow-success
 common-interceptors
 (fn [_ _]
   {:dispatch [:change-route "/pals"]}))

;; Login

(re-frame/reg-event-fx
 :login
 common-interceptors
 (fn [ctx [email password]]
   {:http-xhrio {:method          :post
                 :uri             (palr.util/api "/login")
                 :timeout         8000
                 :params          {:email email :password password}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:login-success]
                 :on-failure      [:login-failure]}}))

(re-frame/reg-event-db
 :login-success
 common-interceptors
 (fn [db [{:keys [accessToken]}]]
   (assoc-in db [:session :access-token] accessToken)))

(re-frame/reg-event-db
 :login-failure
 common-interceptors
 (fn [db event]
   (println "Login failure" event)
   db))

;; Register

(re-frame/reg-event-fx
 :register
 common-interceptors
 (fn [ctx [name email password location]]
   {:http-xhrio {:method          :post
                 :uri             (palr.util/api "/register")
                 :timeout         8000
                 :params          {:name name :email email :password password :location location}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:register-success]
                 :on-failure      [:register-failure]}}))

(re-frame/reg-event-db
 :register-success
 common-interceptors
 (fn [db [{:keys [accessToken]}]]
   (assoc-in db [:session :access-token] accessToken)))


(re-frame/reg-event-db
 :register-failure
 common-interceptors
 (fn [db event]
   (println "Register failure" event)
   db))

;; Fetch Conversations

(re-frame/reg-event-fx
 :fetch-conversations
 common-interceptors
 (fn [{db :db} _]
   (let [access-token (-> db :session :access-token)]
     {:http-xhrio {:method          :get
                   :uri             (palr.util/api "/conversations")
                   :timeout         8000
                   :headers         {:authorization access-token}
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:fetch-conversations-success]
                   :on-failure      [:fetch-conversations-failure]}})))

(re-frame/reg-event-db
 :fetch-conversations-success
 common-interceptors
 (fn [db [conversations]]
   (assoc db :conversations conversations)))

(re-frame/reg-event-db
 :fetch-conversations-failure
 common-interceptors
 (fn [db event]
   (println "Failed to fetch conversations" event)
   db))

;;

(re-frame/reg-event-db
 :reset-router-params
 common-interceptors
 (fn [db [params]]
   (assoc db :router-params params)))
