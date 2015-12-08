(ns fdc-ts.core
  (:use [korma db core] fdc-ts.db)
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET PUT POST]]
            [compojure.route :as route]))


;DESIGN-Prinzip: Alles extrem simpel und einfach halten!!


(defentity coverage_data)

(defn- insert-coverage []
  (insert coverage_data (values {:project_name "foo" :subproject "bar" :language "clojure"})))

(defresource put-coverage []
  :available-media-types ["application/json"]
  :allowed-methods [:put]
  :put! (fn [ctx] (insert-coverage)))

;Daten aus put entgegennehmen und in sqlite-db ablegen, aktueller Tag als key (wenn Daten schon vorhanden -> replace, wenn Wert größer)
;erwartetes JSON: {lines: <n>, covered: <m>, project: <project-name>, sub-project: <sub-project>, language: <java|javascript|go>}


;TODO
;GET /statistic/coverage/<project-name> -> Aggregierte Coverage-Daten für Projekt <project-name> rausgeben
;    -> Liest für jedes Teilprojekt aus der Cassandra die Wert summiert sie und gibt den aggregierten Wert zurück
;PUT /project/<project-name>/subprojects -> Liste von Teilprojekten dem stat-server bekannt machen (braucht man für das GET)
;

;UI -> Beliebig baubar gegen die API, web-ui mit reagent o.ä.


(defroutes app
  (PUT "/data/coverage" [] (put-coverage)))

(def handler
  (-> app wrap-params))
