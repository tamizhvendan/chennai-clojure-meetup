(ns core
  (:require [honeyeql.core :as heql]
            [infra.db :as db]
            [reitit.ring :as ring]
            [ring.util.response :as http]
            [ring.adapter.jetty :as jetty]
            [clojure.data.json :as json]
            [clj-http.client :as http-client]))

(defn- fetch-developer [id]
  (heql/query-single db/adapter {[:developer/id id]
                                 [:developer/github-handle]}))

(defn- fetch-github-profile [github-handle]
  (let [url (format "https://api.github.com/users/%s" github-handle)]
    (-> (http-client/get url)
        :body
        json/read-str
        (select-keys ["name" "location" "followers" "company"]))))

(defn- get-developer [id]
  (-> (fetch-developer id)
      :developer/github-handle
      fetch-github-profile
      (assoc :updated-at (str (java.time.OffsetDateTime/now)))))

(defn- get-developer-handler [req]
  (-> (get-in req [:path-params :id])
      Integer/parseInt
      get-developer
      json/write-str
      http/response))

(defn- router []
  (ring/router ["/developers/:id" {:get get-developer-handler}]))

(defn start-server []
  (jetty/run-jetty (ring/ring-handler (router))
                   {:join? false
                    :port  4567}))

(defn -main []
  (start-server))

(comment
  (get-developer 3)
  (def server (start-server))
  (.stop server))