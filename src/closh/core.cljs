(ns closh.core
  (:require [clojure.string]))
  ; (:require [cljs.reader :refer [read-string]]))
  ; (:require-macros [closh.core :refer [sh]]))

(def spawn (.-spawn (js/require "child_process")))
(def es (js/require "event-stream"))
(def glob (.-sync (js/require "glob")))

; (defn read-command [input]
;   (let [s (if (re-find #"^\s*#?\(" input)
;             input
;             (str "(sh " input ")"))]
;     (read-string s)))

;options
; env
; cwd

(defn expand [s]
  (if (re-find #"^\$" s)
    (if-let [val (aget js/process.env (subs s 1))]
      (expand val)
      (list))
    (-> s
      (clojure.string/replace-first #"^~" (.-HOME js/process.env))
      (glob #js{:nonull true}))))

(defn shx [cmd & args]
  (spawn cmd (apply array (flatten args))))

(defn get-out-stream [x]
  (or (.-stdout x)
      (-> x :out (.pipe (.join es "\n")))))

(defn get-data-stream [x]
  (or (:out x)
      (-> x .-stdout (.pipe (.split es "\n")))))

(defn create-data-stream [x]
  {:out x})

(defn pipe [& processes]
  (doseq [[from to] (partition 2 1 processes)]
    (-> from
        get-out-stream
        (.pipe (.-stdin to))))
  (last processes))

(defn pipe-map [proc f]
  (-> proc
      get-data-stream
      (.pipe (.map es (fn [data cb]
                        (cb nil (f data)))))
      create-data-stream))

(defn pipe-filter [proc f]
  (-> proc
      get-data-stream
      (.pipe (.map es (fn [data cb]
                        (if (f data)
                          (cb nil data)
                          (cb)))))
      create-data-stream))
