(ns fdc-ts.projects
  (:use fdc-ts.db)
  (:require [korma.core :as sql]))

;; interfacing with db

(sql/defentity projects)

(defn lookup-project [data]
  (first (sql/select projects (sql/where {:project (:project data)
                                          :subproject (:subproject data)
                                          :language (:language data)}))))

(def project-exists? (comp boolean lookup-project))

(defn add-project [data]
    (println "data" data)
    (sql/insert projects (sql/values (select-keys data [:project :subproject :language]))))

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
    (map format-project (group-by :project (sql/select projects)))})

;; validation

(def +project-field-pattern+ #"[\w\-]+")

(defn valid-project-field-string [str]
  (and str (re-matches +project-field-pattern+ str)))

(defn validate-project-data [project-data]
   (and (valid-project-field-string (:project project-data))
        (valid-project-field-string (:subproject project-data))
        (valid-project-field-string (:language project-data))))
