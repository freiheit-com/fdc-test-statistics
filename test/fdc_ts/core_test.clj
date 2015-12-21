(ns fdc-ts.core-test
  (:require [clojure.test :refer :all]
            [fdc-ts.core :refer :all]))

;;;- get-json-body

(deftest should-parse-json-from-context
  (is (= (#'fdc-ts.core/get-json-body {:request {:body (new java.io.StringReader "{\"foo\": 23, \"bar\": \"blubb\"}")}})
         {:foo 23 :bar "blubb"})))
