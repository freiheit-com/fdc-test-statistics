(ns fdc-ts.db
  (:require [environ.core :refer [env]]
            [korma.core :as korma-core]
            [korma.db :as korma-db]
            [schema.core :as s]))

; load db config to configure database connector from environment vars
(def db-connection-info (korma-db/mysql
                          {:host (env :db-host)
                           :port (env :db-port)
                           :db (env :db-schema)
                           :user (env :db-user)
                           :password (env :db-pass)}))

(korma-db/defdb db db-connection-info)

(def Meta-Wire {(s/required-key :projects)
                [{(s/required-key :project) s/Str
                  (s/required-key :subprojects)
                                            [{(s/required-key :subproject) s/Str
                                              (s/required-key :languages)
                                                                           [{:language s/Str}]}]}]})

(def +project-name-pattern+ "[\\w\\-]+")
(def +project-path-pattern+ (re-pattern +project-name-pattern+))
(def +project-field-pattern+ (re-pattern (str "^" +project-name-pattern+ "$")))

(def ProjectField +project-field-pattern+)
(def Project-JSON {:project ProjectField :subproject ProjectField :language ProjectField})
