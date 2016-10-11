(ns palr.handlers
  (:require [re-frame.core :as re-frame]
            [palr.db :as db]
            [secretary.core :as secretary]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

;; https://github.com/gf3/secretary/issues/23
(defn set-hash!
  "Set the location hash of a js/window object."
  ([v] (set-hash! (.-location js/window) v))
    ([location v] (aset location "hash" v)))

(re-frame/reg-event-db
 :change-route
 (fn [db [_ hash]]
   (do
     (secretary/dispatch! hash)
     (set-hash! hash)
     db)))

(re-frame/reg-event-fx
 :login
 (fn [ctx [_ username password]]
   {:http-xhrio {:method          :post
                 :uri             "/login"
                 :timeout         8000
                 :params          {:username username :password password}
                 :format          (ajax/json-request-format)   ;; optional see API docs
                 :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                 :on-success      [:login-success]
                 :on-failure      [:login-failure]}}))

(re-frame/reg-event-fx
 :register
 (fn [ctx [_ username password]]
   {:http-xhrio {:method          :post
                 :uri             "/register"
                 :timeout         8000
                 :params          {:username username :password password}
                 :format          (ajax/json-request-format)   ;; optional see API docs
                 :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                 :on-success      [:register-success]
                 :on-failure      [:register-failure]}}))


(re-frame/reg-event-db
 :register-success
 (fn [db _]
   (println "Register success" _)
   db))


(re-frame/reg-event-db
 :register-failure
 (fn [db _]
   (println "Register failure" _)
   db))

(re-frame/reg-event-db
 :login-success
 (fn [db _]
   (println "Login success" _)
   db))


(re-frame/reg-event-db
 :login-failure
 (fn [db _]
   (println "Login failure" _)
   db))
