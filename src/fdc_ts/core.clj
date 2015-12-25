(ns fdc-ts.core
  (:gen-class)
  (:use fdc-ts.common fdc-ts.config fdc-ts.statistics.latest fdc-ts.db cheshire.core)
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET PUT POST]]
            [compojure.route :as route]
            [clj-time [core :as t][coerce :as tc][format :as tf]]
            [korma.core :refer :all]))

;DESIGN-Prinzip: Alles extrem simpel und einfach halten!!
;DESGIN-Prinzip 2: Rest-API sollte über curl bedienbar sein

(defentity projects)

(defentity coverage_data
  (belongs-to projects))

(defn- get-json-body [ctx]
  (parse-string (slurp (get-in ctx [:request :body])) true))

(defn- add-today-timestamp [map]
  (assoc map :timestamp (t/today-at 00 00)))

(defn- coverage-query-today [data]
  (add-today-timestamp {:projects.project (:project data)
                        :projects.subproject (:subproject data)
                        :projects.language (:language data)}))

(defn- coverage-for-today-exist? [data]
  (> (count (select coverage_data
                (with projects)
                (where (coverage-query-today data)))) 0))

(defn- lookup-project [data]
  (first (select projects (where {:project (:project data)
                                  :subproject (:subproject data)
                                  :language (:language data)}))))

;TODO Validate data (lines, covered)!!!
(defn- insert-coverage [data]
  (let [project (lookup-project data)
        coverage-data (select-keys data [:covered :lines])]
    (if (coverage-for-today-exist? data)
      (update coverage_data (set-fields coverage-data) (where {:projects_id (:id project)}))
      (insert coverage_data (values (add-today-timestamp (assoc coverage-data :projects_id (:id project))))))))

(defn- select-latest-coverage-data [project]
  (let [end-today (t/today-at 23 59)]
    (select coverage_data (with projects)
                          (where {:projects.project project
                                  :timestamp [between [(t/minus end-today (t/months 1)) end-today]]})
                                  ;we look back at most one month to, to ensure O(1) time complexity for statistic calculation
                          (order :timestamp :DESC))))

(def project-exists? (comp boolean lookup-project))

;TODO Validate data!!!
(defn- add-project [data]
  (insert projects (values (select-keys data [:project :subproject :language]))))

(defn- get-all-projects []
  {:projects
    (select projects
      (fields :project)
      (modifier "DISTINCT"))})

;TODO Extend to this format:
;-> {"projects": [{"project": "foo",
;                  "subprojects": [{"subproject": "bar",
;                                   "languages": [{"language": "java"}, {"language": "clojure"}]},
;                                  {"subproject": "baz", "languages": ...})))

;TODO Move DB stuff to separate package
;TODO Move put to separate module

(defn- auth [token ctx]
  (= (token *config*) (get-in ctx [:request :headers "auth-token"])))

(defn- auth-configured [token ctx]
  (token *config*))

(def auth-publish (partial auth :auth-token-publish))
(def auth-publish-configured (partial auth-configured :auth-token-publish))

(def auth-statistics (partial auth :auth-token-statistics))
(def auth-statistics-configured (partial auth-configured :auth-token-statistics))

(def auth-meta (partial auth :auth-token-meta))
(def auth-meta-configured (partial auth-meta-configured :auth-token-meta))

(defn- check-project-and-store-parsed-json [p ctx]
  (let [json (get-json-body ctx)] (if (not (p json)) false {:json json})))

(defresource put-coverage []
  :available-media-types ["application/json"]
  :allowed-methods [:put]
  :service-available? auth-publish-configured
  :authorized? auth-publish
  :allowed? (partial check-project-and-store-parsed-json project-exists?)
  :put! (fn [ctx] (insert-coverage (:json ctx))))

(defresource get-project-coverage-statistic [project]
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :service-available? auth-statistics-configured
  :authorized? auth-statistics
  :handle-ok (fn [_] (generate-string (project-coverage-statistics (select-latest-coverage-data project)))))

(defresource put-project []
  :available-media-types ["application/json"]
  :allowed-methods [:put]
  :service-available auth-meta-configured
  :authorized? auth-meta
  :allowed? (partial check-project-and-store-parsed-json (comp not project-exists?))
  :put! (fn [ctx] (add-project (:json ctx))))

(defresource get-projects []
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :service-available auth-meta-configured
  :authorized? auth-meta
  :handle-ok (fn [_] (generate-string (get-all-projects))))

(defroutes app
  (PUT "/publish/coverage" [] (put-coverage))
  (GET ["/statistics/coverage/latest/:project" :project #"\w+"] [project] (get-project-coverage-statistic project))
  (PUT ["/meta/project"] [] (put-project))
  (GET ["/meta/projects"] [] (get-projects)))

(def handler
  (-> app wrap-params))
