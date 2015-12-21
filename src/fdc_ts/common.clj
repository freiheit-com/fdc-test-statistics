(ns fdc-ts.common)

(defn coverage-percentage [data]
  (let [lines (:lines data)]
    (if (<= lines 0)
      1.0 ;by definition
      (double (/ (:covered data) lines)))))
