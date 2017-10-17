(ns fdc-ts.core
  (:gen-class)
  (:require [fdc-ts.common :refer :all]
            [fdc-ts.db :refer :all]
            [fdc-ts.statistics.latest :refer :all]
            [fdc-ts.statistics.diff :refer :all]
            [fdc-ts.statistics.db :refer :all]
            [fdc-ts.projects :refer :all]
            [environ.core :refer [env]]
            [taoensso.timbre :refer [log logf spy]]
            [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.cors :refer [wrap-cors]]
            [compojure.core :refer [defroutes ANY GET PUT POST context]]
            [compojure.route :as route]
            [cheshire.core :as json]
            [clj-time [core :as t][coerce :as tc][format :as tf][predicates :as tp]]
            [schema.core :as s]))

(defn- get-json-body [ctx]
  (json/parse-string (slurp (get-in ctx [:request :body])) true))

(defn- today-date []
  (t/today-at 23 59))

(defn- previous-weekday [date]
  (cond (tp/monday? date) (t/minus date (t/days 3))
        (tp/sunday? date) (t/minus date (t/days 2))
        :else (t/minus date (t/days 1))))

(defn- weekday-days-back [date days]
  (previous-weekday (t/minus date (t/days (- days 1)))))

;; should be private, but we can't with-redefs-fn *and* refer to this by symbol at the same time
(defn project-diff-date
  "diffs statistics for PROJECT today and the given DATE"
  [project date]
  (let [old (project-coverage-statistics (select-most-recent-coverages-at date project :all :all))
        newd (project-coverage-statistics (select-most-recent-coverages project :all :all))]
    (project-coverage-diff old newd)))

(defn- project-diff-days
  "diffs statistics for PROJECT between today and (- today days). Uses the last weekday."
  [project days]
  (project-diff-date project (weekday-days-back (today-date) days)))

(defn- handle-coverage-request
  [time project subproject language]
  (let [data (select-most-recent-coverages-at time project subproject language)]
    (json/generate-string (project-coverage-statistics data))))

;TODO Move put to separate module

(defn- auth [token project-tokens project ctx]
  (let [request-token  (get-in ctx [:request :headers "auth-token"])]
    (or (= (token env) request-token)
        (= (get-in (json/parse-string (project-tokens env))  [project]) request-token))))

(defn- auth-configured [token ctx]
  (token env))

(def auth-publish (partial auth :auth-token-publish :auth-token-project nil))
(def auth-publish-configured (and(partial auth-configured :auth-token-publish)
                              (partial auth-configured :auth-token-project)))

(def auth-statistics (partial auth :auth-token-statistics :auth-token-project))
(def auth-statistics-configured (and (partial auth-configured :auth-token-statistics)
                                 (partial auth-configured :auth-token-project)))

(def auth-meta (partial auth :auth-token-meta "{}" nil))
(def auth-meta-configured (partial auth-configured :auth-token-meta))

(def project-malformed? (comp not validate-project-data :json))

(defn- json-body [ctx]
  {:json (get-json-body ctx)})


(defresource put-coverage []
  :initialize-context json-body
  :available-media-types ["application/json"]
  :allowed-methods [:put]
  :service-available? auth-publish-configured
  :authorized? auth-publish
  :allowed? (comp main-project-exists? :json)
  :put! (fn [ctx]
          (when (not (project-exists? (:json ctx))) (add-project (:json ctx)))
          (insert-coverage (:json ctx))))

(defresource get-project-coverage-statistic [time project subproject language]
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :service-available? auth-statistics-configured
  :authorized? (partial auth-statistics project)
  :handle-ok (fn [_] (handle-coverage-request time project subproject language)))

(defresource get-project-coverage-diff [project days]
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :service-available? auth-statistics-configured
  :authorized? (partial auth-statistics project)
  :handle-ok (fn [_] (json/generate-string (project-diff-days project days))))

(defresource put-project []
  :initialize-context json-body
  :available-media-types ["application/json"]
  :allowed-methods [:put]
  :service-available auth-meta-configured
  :malformed? project-malformed?
  :authorized? auth-meta
  :allowed? (comp not project-exists? :json)
  :put! (comp add-project :json))

(defresource get-projects []
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :service-available auth-meta-configured
  :authorized? auth-meta
  :handle-ok (fn [_] (json/generate-string (s/validate Meta-Wire (get-all-projects)))))

(defroutes app
  (PUT "/publish/coverage" [] (put-coverage))

  (context ["/statistics/coverage/latest/:project" :project +project-path-pattern+] [project]
           (GET ["/"] []
                (get-project-coverage-statistic (today-date) project :all :all))
           (context ["/:subproject" :subproject +project-path-pattern+] [subproject]
                    (GET ["/"] []
                         (get-project-coverage-statistic (today-date) project subproject :all))
                    (GET ["/:language" :language +project-path-pattern+] [language]
                         (get-project-coverage-statistic (today-date) project subproject language))))

  (context ["/statistics/coverage/:time"] [time]
           (context ["/:project" :project +project-path-pattern+] [project]
                    (GET ["/"] []
                         (get-project-coverage-statistic (tf/parse time) project :all :all))
                    (context ["/:subproject" :subproject +project-path-pattern+] [subproject]
                             (GET ["/"] []
                                  (get-project-coverage-statistic (tf/parse time) project subproject :all))
                             (GET ["/:language" :language +project-path-pattern+] [language]
                                  (get-project-coverage-statistic (tf/parse time) project subproject language)))))

  (context ["/statistics/diff/coverage/:project" :project +project-path-pattern+] [project]
           (GET ["/"] []
                (get-project-coverage-diff project 1))
           (context ["/days/:days" :days #"\d+"] [days]
                    (get-project-coverage-diff project (Integer/parseInt days))))

  (PUT ["/meta/project"] [] (put-project))
  (GET ["/meta/projects"] [] (get-projects))
  (route/files "/" {:root "ui"}))

(def handler
  (-> app
      wrap-params
      (wrap-cors :access-control-allow-origin [#"http://localhost:3449"]
                 :access-control-allow-methods [:get :put :post :delete])))
