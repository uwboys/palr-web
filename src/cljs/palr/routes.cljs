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
  (defroute "/" []
    (re-frame/dispatch [:require-sap signed-out :palr.views/landing "/pals"]))

  (defroute "/login" []
    (re-frame/dispatch [:require-sap signed-out :palr.views/login "/pals"]))

  (defroute "/register" []
    (re-frame/dispatch [:require-sap signed-out :palr.views/register "/pals"]))

  (defroute "/palr-me" []
    (re-frame/dispatch [:require-sap signed-in :palr.views/palr-me "/"]))

  (defroute "/pals" []
    (re-frame/dispatch [:require-sap signed-in :palr.views/pals "/"]))

  (defroute "*" []
    (re-frame/dispatch [:change-route "/"]))

  ;; --------------------
  (hook-browser-navigation!))
