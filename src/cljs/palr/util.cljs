(ns palr.util
  (:require [cljs.reader]
            [palr.constant :as pc]))

(def ^:const default-storage (.-localStorage js/window))

(defn save-to-storage!
  ([key v] (save-to-storage! default-storage key v))
  ([storage key v] (->> (pr-str v) (.setItem storage key))))

(defn fetch-from-storage!
  ([key] (fetch-from-storage! default-storage key))
  ([storage key] (-> (.getItem storage key) str (cljs.reader/read-string))))

(defn fetch-session-from-storage! []
  (fetch-from-storage! pc/session-name))

(defn save-session-to-storage! [session]
  (save-to-storage! pc/session-name session))

;; https://github.com/gf3/secretary/issues/23
(defn set-hash!
  "Set the location hash of a js/window object."
  ([v] (set-hash! (.-location js/window) v))
  ([location v] (aset location "hash" v)))