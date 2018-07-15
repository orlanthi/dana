(ns dana.paradise-sql.core
  "Provides functions to access nodes and edges loaded from a mysql Paradise Papers database"
  (:require [clojure.java.jdbc :as jdbc]))

(def default-db-spec
  {:dbtype "mysql"
   :dbname "paradise"
   :user "root"
   :password "password"
   :useSSL false
   :serverTimezone "UTC"})

(defn edges
  ;; TODO: Consider making this streaming
   "Returns all the edges from the database given by the db-spec (or the default one if none is provided)"
  ([db-spec]
   (jdbc/query db-spec "select * from edges"))
  ([]
   (edges default-db-spec)))

(defn nodes
  ;; TODO: Consider making this streaming
  "Returns all the nodes from the database given by the db-spec (or the default one if none is provided)"
  ([db-spec]
   (let [table-prefixes ["address" "entity" "intermediary" "officer" "other"]
         table-queries (map (fn [prefix]
                              (str "select '" prefix "' as node_type, `nodes." prefix "`.* from `nodes." prefix "`"))
                            table-prefixes)
         query (clojure.string/join " UNION " table-queries)]
     (jdbc/query db-spec query)))
  ([]
   (nodes default-db-spec)))
