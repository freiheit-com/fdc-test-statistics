(ns fdc-ts.config
  (:require [clojure.edn :as edn]))

(def +config-system-property+ "fdc.ts.config.file")

(defn- read-config []
  (let [config-file (System/getProperty +config-system-property+)]
    (if (not config-file)
      (do (println "WARNING: property " +config-system-property+ " not set") {})
      (edn/read-string (slurp config-file)))))

(def ^:dynamic *config* (read-config))
