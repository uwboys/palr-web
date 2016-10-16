(ns palr.routes
    (:require-macros [secretary.core :refer [defroute]])
    (:import goog.History)
    (:require [secretary.core :as secretary]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [re-frame.core :as re-frame]))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn has-no-conversations? [db]
  (-> db :conversations empty?))

(defn signed-out? [db]
  (-> db :session :access-token empty?))

(defn has-temporary-conversation? [db]
  (->> db :conversations (every? :isPermanent) not))

(def signed-in? (complement signed-out?))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  ;; --------------------
  ;; define routes here
  (defroute "/" {:as params}
    (re-frame/dispatch [:cond-sap [signed-in? "/pals"] :palr.views/landing])
    (re-frame/dispatch [:reset-router-params params]))

  (defroute "/login" {:as params}
    (re-frame/dispatch [:cond-sap [signed-in? "/pals"] :palr.views/login])
    (re-frame/dispatch [:reset-router-params params]))

  (defroute "/register" {:as params}
    (re-frame/dispatch [:cond-sap [signed-in? "/pals"] :palr.views/register])
    (re-frame/dispatch [:reset-router-params params]))

  (defroute "/palr-me" {:as params}
    (re-frame/dispatch [:cond-sap [signed-out? "/" has-temporary-conversation? "/pals"] :palr.views/palr-me])
    (re-frame/dispatch [:reset-router-params params]))

  (defroute "/pals/:id" {:as params}
    (re-frame/dispatch [:cond-sap [signed-out? "/" has-no-conversations? "/palr-me"] :palr.views/pals])
    (re-frame/dispatch [:reset-router-params params]))

  (defroute "/pals" {:as params}
    (re-frame/dispatch [:cond-sap [signed-out? "/" has-no-conversations? "/palr-me"] :palr.views/pals])
    (re-frame/dispatch [:reset-router-params params]))

  (defroute "*" {:as params}
    (re-frame/dispatch [:change-route "/"])
    (re-frame/dispatch [:reset-router-params params]))

  ;; --------------------
  (hook-browser-navigation!))
