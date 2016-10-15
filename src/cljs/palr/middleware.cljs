(ns palr.middleware
  (:require [palr.util]
            [re-frame.core :as re-frame]
            [palr.constant]))

(def persist-session!
  (re-frame/after
   (fn [db _]
     (palr.util/save-session-to-storage! (get db :session {})))))
