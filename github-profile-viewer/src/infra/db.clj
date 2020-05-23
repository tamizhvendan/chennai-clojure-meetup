(ns infra.db
  (:require [honeyeql.db :as heql-db]))

(def adapter (heql-db/initialize {:dbtype   "postgres"
                                  :dbname   "devvr"
                                  :user     "postgres"
                                  :password "postgres"}))