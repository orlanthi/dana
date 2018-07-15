(ns dana.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [dana.graph.core]))

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

     (GET "/shortest-paths" []
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
       
       (ok {:result #_[]  [{:node-id from-node-id, :description "node"}
                           {:relationship-type "link to"}
                           {:node-id to-node-id, :description "node"}]})))))
