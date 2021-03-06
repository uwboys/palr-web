(ns palr.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 :router-params
 (fn [db _]
   (:router-params db)))

(re-frame/reg-sub
 :conversations
 (fn [db _]
   (:conversations db)))

(re-frame/reg-sub
 :messages
 (fn [db _]
   (:messages db)))

(re-frame/reg-sub
 :progress
 (fn [db _]
   (:progress db)))

(re-frame/reg-sub
 :session
 (fn [db _]
   (:session db)))

(re-frame/reg-sub
 :profile-open?
 (fn [db _]
   (:profile-open? db)))

(re-frame/reg-sub
 :pal-profile-open?
 (fn [db _]
   (:pal-profile-open? db)))
