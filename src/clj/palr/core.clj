(ns palr.core
  (:require [compojure.core :refer :all]
            [ring.util.response :refer [response status]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [clojure.pprint :refer [pprint]]))

(defn uuid [] (str (java.util.UUID/randomUUID)))
(def tokens (atom {["ramanpreet" "isawesome"] (uuid)}))

(defroutes api
  (POST "/login" {{:keys [username password]} :body}
    (let [creds [username password]]
      (pprint creds)
      (if (contains? @tokens creds)
        (-> {:access-token (@tokens creds)}
            (response)
            (status 200))
        (-> {:error "Invalid credentials."}
            (response)
            (status 422)))))
  (POST "/register" {{:keys [username password]} :body}
    (let [creds [username password]]
      (if (every? #(-> % count pos?) creds)
        (let [id (uuid)]
          (swap! tokens assoc creds id)
          (-> {:access-token (@tokens creds)}
              (response)
              (status 201)))
        (-> {:error "Invalid credentials."}
            (response)
            (status 422))))))

(def app
  (-> api
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-json-response)
      (wrap-defaults api-defaults)))

(def dev-handler (-> #'app wrap-reload))

(def handler app)
