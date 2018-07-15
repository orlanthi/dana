(ns dana.graph.ubergraph
  (:require [ubergraph.core :as uber]
            [ubergraph.alg :as alg]
            [clojure.spec.alpha :as s]))

(defn graph
  "Returns an empty graph"
  []
  (uber/multidigraph))

(s/def ::n.node_id integer?)
(s/def ::node_1 ::n.node_id)
(s/def ::node_2 ::n.node_id)
(s/def ::node (s/keys :req-un [::n.node_id]))
(s/def ::nodes (s/* ::node))
(s/def ::edge (s/keys :req-un [::node_1 ::node_2]))
(s/def ::edges (s/* ::edge))

(defn integer->node-id [i]
  (keyword (str "node" i)))

(defn pre-valid-graph [graph] (s/valid? (s/and (s/spec some?)
                                               (s/spec uber/ubergraph?)) graph))

(defn add-nodes

  "Returns a new graph with the given set of nodes applied to it. The nodes are expected to be maps with an identifying key of :n.node_id. The given node will then be identified by a keyword like ':node123'."

  [graph nodes]

  {:pre [(pre-valid-graph graph)
         (s/valid? ::nodes nodes)]}

  (uber/add-nodes-with-attrs* graph
                              (map (fn [node] [(integer->node-id (:n.node_id node)) node])
                                   nodes)))

(defn add-edges 

  "Returns a new graph with the given set of edges applied to it. The edges are expected to be maps with keys identifying the two nodes, node_1 and node_2."
  
  [graph edges]

  {:pre [(pre-valid-graph graph)
         (s/valid? ::edges edges)]}

  (uber/add-directed-edges* graph
                            (map (fn [edge] [(integer->node-id (:node_1 edge))
                                             (integer->node-id (:node_2 edge))
                                             edge])
                                 edges)))

(defn- node->edn [graph node]
  (let [attrs (uber/attrs graph node)]
    {:id (:n.node_id attrs)
     :name (:n.name attrs)}))

(defn- edge->edn [graph edge]
  (let [attrs (uber/attrs graph edge)]
    {:relationship-type (:rel_type attrs)}))

(defn- path->edn [graph [first-edge & rest-edges]]
  (let [node-and-edge [(node->edn graph (uber/src first-edge))
                       (edge->edn graph first-edge)]]
    (concat node-and-edge
            (if (nil? rest-edges)
              [(node->edn graph (uber/dest first-edge))]
              (path->edn graph rest-edges)))))

(defn shortest-path
  
  "Returns the shortest path between two-nodes."
  
  [graph node1 node2]

  {:pre [(pre-valid-graph graph)
         (s/valid? ::n.node_id node1)
         (s/valid? ::n.node_id node2)]}

  (let [path (alg/shortest-path graph
                                (integer->node-id node1)
                                (integer->node-id node2))]
    (if (nil? path)
      []
      (let [edges (alg/edges-in-path path)]
        (if (empty? edges)
          []
          (path->edn graph edges))))))

#_(def my-graph (-> (graph)
                  (add-nodes (dana.paradise-sql.core/nodes))
                  (add-edges (dana.paradise-sql.core/edges))))

#_(shortest-path my-graph 84100000 81001128)
