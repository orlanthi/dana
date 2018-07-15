(ns dana.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [dana.graph.core]
            [dana.graph.neo4j]
            [dana.graph.ubergraph]
            [dana.paradise-sql.core]))

(def graph (atom nil))

(defn load-ubergraph []
  (swap! graph (fn [old]
                 (-> (dana.graph.ubergraph/graph)
                     (dana.graph.ubergraph/add-nodes (dana.paradise-sql.core/nodes))
                     (dana.graph.ubergraph/add-edges (dana.paradise-sql.core/edges)))))
  nil)

(defn load-graph []
  (load-ubergraph))

(def app
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :data {:info {:title "Dana"
                   :description "Paradise Papers Query API"}
            :tags [{:name "api", :description "API operartions"}]}}}

   (context "/api" []
     :tags ["api"]
     :coercion :spec

     (GET "/neo4j/shortest-path" []
       :no-doc false
       :description (str "Returns the shortest path between two 'nodes' in the Paradise Papers. "
                         "If no path can be found then the output will be an empty list. "
                         "If a path is found then it will be output as an alternating  sequence of "
                         "Nodes and Edges, starting with the start node provided and finishing with "
                         "the end node.")
       ;; Only reason this is commented out is because the schema description for the output is poor.
       ;; I've tried to change no-doc to true and specify the paths explicitly in the swagger section but
       ;; can't seem to make that work either. (TODO: contact metosin)
       #_:return #_{:result :dana.graph.core/node-path }
       :query-params [from-node-id :- :dana.graph.core.node/node-id
                      to-node-id :- :dana.graph.core.node/node-id]
       
       (ok {:result (dana.graph.neo4j/shortest-path (dana.graph.neo4j/conn) from-node-id to-node-id)}))

     (GET "/ubergraph/shortest-path" []
       :no-doc false
       :description (str "Returns the shortest path between two 'nodes' in the Paradise Papers. "
                         "If no path can be found then the output will be an empty list. "
                         "If a path is found then it will be output as an alternating  sequence of "
                         "Nodes and Edges, starting with the start node provided and finishing with "
                         "the end node.")
       ;; Only reason this is commented out is because the schema description for the output is poor.
       ;; I've tried to change no-doc to true and specify the paths explicitly in the swagger section but
       ;; can't seem to make that work either. (TODO: contact metosin)
       #_:return #_{:result :dana.graph.core/node-path }
       :query-params [from-node-id :- :dana.graph.core.node/node-id
                      to-node-id :- :dana.graph.core.node/node-id]
       
       (do
         (if (nil? @graph) (load-graph))
         (ok {:result (dana.graph.ubergraph/shortest-path @graph from-node-id to-node-id)}))))))
