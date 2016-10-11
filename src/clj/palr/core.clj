(ns palr.core
  (:require [compojure.core :refer :all]
            [ring.util.response :refer [response status]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [clojure.pprint :refer [pprint]]))

(def users #{["ramanpreet" "isawesome"]})
(def tokens {["ramanpreet" "isawesome"] "Ramanpreet's special token!"})

(defroutes api
  (POST "/login" {{:keys [username password]} :body}
    (let [creds [username password]]
      (pprint creds)
      (if (contains? users creds)
        (-> {:access-token (tokens creds)}
            (response)
            (status 200))
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
