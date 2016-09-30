(ns palr.handlers
  (:require [re-frame.core :as re-frame]
            [palr.db :as db]
            [secretary.core :as secretary]))

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
