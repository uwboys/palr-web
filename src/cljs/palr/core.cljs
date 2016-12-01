(ns palr.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [devtools.core :as devtools]
            [js.classnames]
            [palr.handlers]
            [palr.subs]
            [palr.routes :as routes]
            [palr.views :as views]
            [palr.config :as config]
            [palr.ws :as ws]))

(enable-console-print!)

(defn dev-setup []
  (when config/debug?
    (println "dev mode")
    (devtools/install!)))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [:init])
  (dev-setup)
  (mount-root))
