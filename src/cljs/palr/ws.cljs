(ns palr.ws
  (:require [socket.io-client]
            [palr.constant :as pc]))

(defonce socket (atom nil))

(defn emit! [name data]
  (.emit @socket name data))

(defn on! [name fn]
  (.on @socket name fn))

(defn start-client! [db]
  (let [access-token (-> db :session :access-token)
        signed-in? (not (nil? access-token))
        not-connected? (nil? @socket)]
    (when (and signed-in? not-connected?)
      (reset! socket (.connect js/io (str pc/api "/ws")))
      (emit! "token" access-token))))
