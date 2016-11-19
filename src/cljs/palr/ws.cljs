(ns palr.ws
  (:require [js.socket.io-client]
            [palr.constant :as pc]
            [re-frame.core :as re-frame]
            [js.alertify]))

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
      (emit! "add_client" access-token)
      (on! "message" #(re-frame/dispatch [:save-message (js->clj % :keywordize-keys true)]))
      (on! "temporary_match" #(doseq [action [[:toast-success "You were matched"]
                                              [:refresh-user]]]
                                (re-frame/dispatch action)))
      (on! "permanent_match" #(doseq [action [[:toast-success "You have a permanent match!"]
                                              [:fetch-conversations "/"]]]
                                (re-frame/dispatch action))))))
