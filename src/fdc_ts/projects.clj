(ns fdc-ts.projects
  (:use fdc-ts.db)
  (:require [korma.core :as sql]
            [schema.core :as s]
            [taoensso.timbre :refer [log logf]]))

;; interfacing with db

(sql/defentity projects)

(defn lookup-project [{:keys [project subproject language]}]
  (first (sql/select projects (sql/where {:project project
                                          :subproject subproject
                                          :language language
                                          :active true}))))

(defn lookup-main-project [{:keys [project language]}]
  (first (sql/select projects (sql/where {:project project
                                          :language language}))))

(def project-exists? (comp boolean lookup-project))

(def main-project-exists? (comp boolean lookup-main-project))

(defn add-project [{:keys [:project :subproject :language] :as data}]
  (log :info "adding project with " data)
  (sql/insert projects (sql/values {:project project
                                    :subproject subproject
                                    :language language})))

(defn deactivate-project
  "Mark the given PROJECT as inactive."
  [project]
  (sql/update projects
              (sql/set-fields {:active false})
              (sql/where {:project project})))


(defn format-language
  "formats raw data of a LANGUAGE"
  [language]
  (select-keys language [:language]))


(defn format-subproject
  "formats raw data of a SUBPROJECT"
  [[name languages]]
  {:subproject name :languages (map format-language languages)})


(defn format-project
  "formats raw data of a PROJECT"
  [[name subprojects]]
  {:project name :subprojects (map format-subproject (group-by :subproject subprojects))})


(defn get-all-projects []
  "Returns information about all registered projects in the following format:
-> {\"projects\": [{\"project\": \"foo\",
                  \"subprojects\": [{\"subproject\": \"bar\",
                                   \"languages\": [{\"language\": \"java\"}, {\"language\": \"clojure\"}]},
                                  {\"subproject\": \"baz\", \"languages\": ...}"
  {:projects
   (map format-project (group-by :project (sql/select projects (sql/where {:active true}))))})

;; validation

(defn validate-project-data [project-data]
  (nil? (s/check Project-JSON project-data)))
