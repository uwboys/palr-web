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

(defn signed-in [db]
  (-> db :session :access-token empty? not))

(def ^:const signed-out (complement signed-in))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  ;; --------------------
  ;; define routes here
  (defroute "/" {:as params}
    (re-frame/dispatch [:require-sap signed-out :palr.views/landing "/pals"])
    (re-frame/dispatch [:reset-router-params params]))

  (defroute "/login" {:as params}
    (re-frame/dispatch [:require-sap signed-out :palr.views/login "/pals"])
    (re-frame/dispatch [:reset-router-params params]))

  (defroute "/register" {:as params}
    (re-frame/dispatch [:require-sap signed-out :palr.views/register "/pals"])
    (re-frame/dispatch [:reset-router-params params]))

  (defroute "/palr-me" {:as params}
    (re-frame/dispatch [:require-sap signed-in :palr.views/palr-me "/"])
    (re-frame/dispatch [:reset-router-params params]))

  (defroute "/pals/:id" {:as params}
    (re-frame/dispatch [:reset-router-params params])
    (re-frame/dispatch [:require-sap signed-in :palr.views/pals "/"]))

  (defroute "/pals" {:as params}
    (re-frame/dispatch [:require-sap signed-in :palr.views/pals "/"])
    (re-frame/dispatch [:reset-router-params params]))

  (defroute "*" {:as params}
    (re-frame/dispatch [:change-route "/"])
    (re-frame/dispatch [:reset-router-params params]))

  ;; --------------------
  (hook-browser-navigation!))
