(ns palr.util
  (:require [cljs.reader]
            [palr.constant :as pc]
            [cljs-time.core :as ct.core]
            [cljs-time.coerce :as ct.coerce]
            [cljs-time.format :as ct.format]))

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

(defn api [uri]
  (let [base-url pc/api]
    (str base-url uri)))

(defn attr-into [d s]
  (if (every? coll? [d s]) (into d s) s))

(defn merge-attrs [dest source]
  (let [dest-class (str (:class dest) " " (:class source))]
    (-> (merge-with attr-into dest source)
        (assoc :class dest-class))))

(defn random!
  ([] (.random js/Math))
  ([n] (* (random!) n))
  ([a b] (+ (random! (- b a)) a)))

(defn same-year? [one two]
  (= (ct.core/year one) (ct.core/year two)))

(defn same-month? [one two]
  (and
    (= (ct.core/month one) (ct.core/month two))
    (same-year? one two)))

(defn same-week? [one two]
  (and
    (= (quot (ct.core/day one) 7) (quot (ct.core/day two) 7))
    (same-month? one two)))

(defn same-day? [one two]
  (and
    (= (ct.core/day one) (ct.core/day two))
    (same-month? one two)))

(def week-day-formatter (ct.format/formatter "EEEE"))
(def time-formatter (ct.format/formatter "h:mm a"))
(def date-formatter (ct.format/formatter "MM/dd/yyyy"))

(defn get-week-date [now other]
  (let [now-day (ct.core/day-of-week now)
        other-day (ct.core/day-of-week other)]
        (if (= 1 (- now-day other-day))
          "Yesterday"
          (ct.format/unparse week-day-formatter other))))

(defn get-date [created-at]
  (let [now (ct.core/date-time (js/Date.))
        other (ct.core/date-time (js/Date. created-at))]
        (cond
          (same-day? now other)   (ct.format/unparse time-formatter other)
          (same-week? now other)  (get-week-date now other)
          :else                   (ct.format/unparse date-formatter other))))
