(ns fdc-ts.projects
  (:require [korma.core :refer :all]))

(defentity projects)

(defn lookup-project [data]
  (first (select projects (where {:project (:project data)
                                  :subproject (:subproject data)
                                  :language (:language data)}))))

(def project-exists? (comp boolean lookup-project))

;TODO Validate data!!!
(defn add-project [data]
  (insert projects (values (select-keys data [:project :subproject :language]))))

(defn get-all-projects []
  {:projects
    (select projects
      (fields :project)
      (modifier "DISTINCT"))})

      ;TODO Extend to this format:)})
      ;-> {"projects": [{"project": "foo",
      ;                  "subprojects": [{"subproject": "bar",
      ;                                   "languages": [{"language": "java"}, {"language": "clojure"}]},
      ;                                  {"subproject": "baz", "languages": ...})))
