(ns tonic.core
  (:require [datomic.api :only (q db) :as d]
            [clojure.java.io :as io]))

(def host "localhost")
(def port "4334")
(def datomic-db "dev-db-1")
(def uri (str "datomic:dev://" host ":" port "/" datomic-db))

;; Setup
;;--------------------------------------------------------------------
(defn open-conn
  "Create a connection to the dev db."
  []
  (d/create-database uri)
  (d/connect uri))

(def conn (d/connect uri))

(defn erase-db [] (d/delete-database uri))

;; Load Schema
;;--------------------------------------------------------------------
(defn schemata
  "Lists resource files of pattern schema-*.edn"
  []
  (filter #(re-matches #"^schema-.+\.edn$" %)
          (->> (file-seq (clojure.java.io/file "./resources"))
               (filter #(.isFile %))
               (map #(.getName %)))))

(defn load-schema
  "Loads a sequence of schamta files into Datomic"
  []
  (doseq [x (vec (schemata))]
    (let [schema (load-file (apply str "resources/" x))]
      @(d/transact conn schema)
      conn)))

(defn list-schema
  []
  (doseq [x (vec (schemata))]
    (let [schema (load-file (apply str "resources/" x))]
      (println schema))))

(defn iter-all-files-lazy [path]
  (lazy-seq
    (cons path
      (when (.isDirectory (java.io.File. path))
        (mapcat
          (comp iter-all-files-lazy #(. % getCanonicalPath))
          (.listFiles (java.io.File. path)))))))

(defn load-schema-old
  "Load schema files from resources. Likely only works with one file now"
  []
  (let [schema (load-file (apply str "resources/" (schemata)))]
    @(d/transact conn schema)
    conn))

(defn quickstart [] (do (open-conn) (load-schema)))
;; Interaction
;;--------------------------------------------------------------------
(defn add-person
  "Adds a Person datom with only the last name."
  [lastName]
  @(d/transact conn [{:db/id (d/tempid :person)
                      :person/lastName lastName}]))

(defn add-person-with-mrn-and-lastName-firstName [mrn lastName firstName]
  @(d/transact conn [{:db/id (d/tempid :person)
                      :person/mrn mrn
                      :person/lastName lastName
                      :person/firstName firstName}]))

(defn find-all-persons
  "This datalog doesn't work."
  []
  (d/q '[:find ?mrn ?lastName ?firstName
         :where
         [_ :person/mrn ?mrn]
         [?e :person/lastName ?lastName]
         [_ :person/firstName ?firstName]]
       (d/db conn)))

(defn show-persons []
  (d/q '[:find ?lastName
         :where [?e :person/lastName ?lastName]]
       (d/db conn)))

(defn show-schema []
  (d/q '[:find ?ident
         :where [_ :db/ident ?ident]]
       (d/db conn)))
