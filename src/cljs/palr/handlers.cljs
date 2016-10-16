(ns palr.handlers
  (:require [re-frame.core :as re-frame]
            [palr.db :as db]
            [secretary.core :as secretary]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
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

(re-frame/reg-event-db
 :require-sap
 common-interceptors
 (fn [db [func panel redirect]]
   (if (func db)
     (re-frame/dispatch [:set-active-panel panel])
     (re-frame/dispatch [:change-route redirect]))
   db))

(re-frame/reg-event-db
 :change-route
 common-interceptors
 (fn [db [hash]]
   (do
     (secretary/dispatch! hash)
     (palr.util/set-hash! hash)
     db)))

(defn api [uri]
  (let [base-url "http://d1f497ce.ngrok.io"]
    (str base-url uri)))

(re-frame/reg-event-fx
 :login
 common-interceptors
 (fn [ctx [email password]]
   {:http-xhrio {:method          :post
                 :uri             (api "/login")
                 :timeout         8000
                 :params          {:email email :password password}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:login-success]
                 :on-failure      [:login-failure]}}))

(re-frame/reg-event-fx
 :register
 common-interceptors
 (fn [ctx [name email password location]]
   {:http-xhrio {:method          :post
                 :uri             (api "/register")
                 :timeout         8000
                 :params          {:name name :email email :password password :location location}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:login-success]
                 :on-failure      [:register-failure]}}))

(re-frame/reg-event-db
 :register-failure
 common-interceptors
 (fn [db _]
   (println "Register failure" _)
   db))

(re-frame/reg-event-db
 :login-success
 common-interceptors
 (fn [db [{:keys [accessToken]}]]
   (re-frame/dispatch [:change-route "/pals"])
   (assoc-in db [:session :access-token] accessToken)))

(re-frame/reg-event-db
 :login-failure
 common-interceptors
 (fn [db _]
   (println "Login failure" _)
   db))

(re-frame/reg-event-db
 :reset-router-params
 common-interceptors
 (fn [db [params]]
   (assoc db :router-params params)))
