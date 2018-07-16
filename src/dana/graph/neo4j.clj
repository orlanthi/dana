(ns dana.graph.neo4j
  (:require [clojurewerkz.neocons.rest :as rest]
            [clojurewerkz.neocons.rest.nodes :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [clojurewerkz.neocons.rest.records :as records]
            [clojurewerkz.neocons.rest.cypher :as cypher]
            [clojure.spec.alpha :as s]))

(defn conn []
  (rest/connect "http://neo4j:password@localhost:7474/db/data/"))

(defn- node->edn [node]
  {:id (get-in node [:data :node_id])
   :name (get-in node [:data :name])})

(defn- edge->edn [edge]
  {:relationship-type (get-in edge [:data :rel_type])})

(defn- interleave-nodes-and-edges [result nodes-remaining edges-remaining]
  (if (empty? edges-remaining)
    (concat result
            [(first nodes-remaining)])
    (concat result
            [(first nodes-remaining)]
            [(first edges-remaining)]
            (interleave-nodes-and-edges result
                                        (drop 1 nodes-remaining)
                                        (drop 1 edges-remaining)))))

(defn path->edn [conn path]
  (let [nodes (map (fn [node-url]
                     (node->edn (nodes/fetch-from conn node-url)))
                   (:nodes path))
        edges (map (fn [relationship-url]
                     (edge->edn (relationships/fetch-from conn relationship-url)))
                   (:relationships path))]
    (interleave-nodes-and-edges [] nodes edges)))

(defn shortest-path [conn node1-id node2-id]
  (let [query (str "MATCH "
                   "(n1 {node_id: "
                   node1-id
                   "}), "
                   "(n2 {node_id: "
                   node2-id
                   "}), "
                   "p=shortestPath((n1)-[*..15]-(n2)) "
                   "RETURN p")
        results (cypher/tquery conn query)]

    (if (empty? results)
      []
      (let [result (first results)]
        (if (nil? result)
          nil
          (let []
            (path->edn conn (result "p"))))))))

(defn populate-graph [conn nodes edges]
  (let [nodes-map (reduce (fn [m node]
                            (let [node-id (:n.node_id node)
                                  neo4j-node (nodes/create conn {:node_id node-id
                                                                 :name (:n.name node)})]
                              (assoc m node-id neo4j-node)))
                          {}
                          nodes)]
    (doseq [edge edges]
      (let [node1 (nodes-map (:node_1 edge))
            node2 (nodes-map (:node_2 edge))]
        (relationships/create conn node1 node2 :link {:rel_type (:rel_type edge)})))))

(defn get-node-by-node-id [conn id]
  (let [results (cypher/tquery conn
                               "MATCH (n) where n.node_id = {node_id} RETURN n"
                               {:node_id id})]
    (if (>(count results) 1)
      (throw (ex-info "Multiple results from find" {:node_id id :count (count results)}))
      (let [result (first results)]
        (if (nil? result)
          nil
          (records/instantiate-record-from (result "n")))))))


;; Naive implemention as per ubergrah. Because we lose the Neo4J node id 
;; information after loading the nodes, we have to reload them through a
;; slow (perhaps not indexed?) http interface.

#_(defn add-nodes

  [conn nodes]

  (doseq [node nodes]
    (nodes/create conn {:node_id (:n.node_id node)
                     :name (:n.name node)})-))

#_(defn add-edges 

  [conn edges]

  (doseq [edge edges]
    (let [node1 (get-node-by-node-id conn (:node_1 edge))
          node2 (get-node-by-node-id conn (:node_2 edge))]
      (relationships/create conn node1 node2 :link {:rel_type (:rel_type edge)}))))

;; To load the database, run the following from the REPL within this namespace
;; It will take somewhere between 30 minutes to an hour to do so.
#_(populate-graph (conn)
                  (dana.paradise-sql.core/nodes)
                  (dana.paradise-sql.core/edges))
