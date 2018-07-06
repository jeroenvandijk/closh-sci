(ns closh.zero.platform.io
  (:require [clojure.string]
            [clojure.java.io :as io]
            [org.satta.glob :as clj-glob]))

(def ^:private relpath-regex #"^\./")

(defn glob [s]
  (let [pattern (clojure.string/replace s relpath-regex "")
        result (clj-glob/glob pattern)]
    (if (seq result)
      (if (= s pattern)
        (map #(clojure.string/replace (str %) relpath-regex "") result)
        (map #(str %) result))
      [s])))

(defn process-output
  "Returns for a process to finish and returns output to be printed out."
  [proc]
  (with-open [writer (java.io.StringWriter.)]
    (io/copy (.getInputStream proc) writer)
    (str writer)))
