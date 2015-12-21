(ns fdc-ts.config)

(defn- read-config []
  (let [config-file (System/getProperty "fdc.ts.config.file")]
    (if (not config-file)
      {}
      {:auth-token-publish "test-foo"})))

(def ^:dynamic *config* (read-config))
