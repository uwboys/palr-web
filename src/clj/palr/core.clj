(ns palr.core
  (:require [compojure.core :refer :all]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [clojure.pprint :refer [pprint]])) ; httpkit is a server

(defroutes api
  (POST "/login" {body :body} (do (pprint body) "what... horses?")))

(def app
  (-> api
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-json-response)
      (wrap-defaults api-defaults)))

(def dev-handler (-> #'app wrap-reload))

(def handler app)
