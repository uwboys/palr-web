(ns palr.db
  (:require [palr.util]))

(def default-db
  {:name "re-frame"
   :session (or (palr.util/fetch-session-from-storage!) {})})
