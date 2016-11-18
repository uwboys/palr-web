(ns palr.db
  (:require [palr.util]))

(def default-db
  {:name "re-frame"
   :session (or (palr.util/fetch-session-from-storage!) {})
   :conversations []
   :messages {}
   :inMatchProcess false
   :isTemporarilyMatched false
   :isPemanentlyMatched false
   :router-params {}
   :progress 100
   :profile-open? false})
