# dana

This is a web application to allow Dana to explore the Paradise Papers dataset. 
The application currently offers two endpoints to query the Paradise Papers to
determine the shortest path between two 'nodes'. The first endpoint used Neo4J
as a graph datastore and the other uses an in-memory datastore implemented with
ubergraph.

Note: This is very much a proof-of-concept version of the basic requirements.
See below for future expansion ideas.

## Setup

You will need a local mysql database loaded with the Paradise Papers MySQL dump.

### OSX

> Assumes you have `brew` installed.

#### MySQL

Install mysql by running:
```
brew install mysql
```

Secure the mysql installation:
```
brew services start mysql
mysql_secure_installation
```
> In reponse to the secure script, answer the following:
> * Would you like to setup VALIDATE PASSWORD plugin?
>   * No
> * Change the password for root ?
>   * Yes
>   * Set it to 'password'
> * Remove anonymous users?
>   * Yes
> * Disallow root login remotely?
>   * Yes
> * Remove test database and access to it?
>   * Yes
> * Reload privilege tables now?
>   * Yes

Test your connection:
```
mysql -uroot -ppasword
exit
```

Assuming you have the paradise.sql file in the parent directory, load the paradise papers database by running:
```
mysql -uroot -ppassword < ../paradise.sql
```

Check the data has loaded:
```
mysql -uroot -ppassword paradise -e "select count(*) from edges;"
```

#### Neo4J

Install Neo4J by running:
```
brew install neo4j
```

Make sure Neo4J is running:
```
brew services start neo4j
```

Login to the Neo4J server (http://localhost:7474/browser/) using usename/password `neo4j` and change the password to `password`.

You will need to load Paradise Papers in the MySQL datastore into the Neo4J datastore if you want to use the main endpoint. You
will only need to do this once. Open a repl by running `lein repl` and run the following:
```
(load-file "src/dana/graph/neo4j.clj")
(load-file "src/dana/paradise_sql/core.clj")
(in-ns 'dana.graph.neo4j)
(populate-graph (conn)
                (dana.paradise-sql.core/nodes)
                (dana.paradise-sql.core/edges))
```
Note that this could take up to an hour to run. 

If you want to clear the Neo4J database you can do so by running the following
from the repl.

> WARNING: The following will clear you Neo4J datastore!

```
(load-file "src/dana/graph/neo4j.clj")
(in-ns 'dana.graph.neo4j)
;; Remove relationships first
(cypher/tquery (conn) "match (n)-[r]->() delete r")
;; Check relationships have been removed
(cypher/tquery (conn) "match (n)-[r]->() return count(r)")
;; Remove nodes
(cypher/tquery (conn) "match (n) delete n")
;; Check nodes have been removed
(cypher/tquery (conn) "match (n) return count(n)")
```

## Usage

### Run the application locally

`lein ring server`

This will open up a web-browser showing a 'swagger' view of the API. Expand the shortest-paths endpoint and enter two node ids, 
eg: 84100000 and 81001128, then hit 'Try it out!'.

### Run the tests

`lein test`

or

`lein test-refresh`

to run them continually during development.

#### Testing TODOs:

* Add 'controller' integration testing.
* Explore using Neo4J in-memory for graph population testing and integration testing.
  (See: https://neo4j.com/docs/stable/tutorials-java-unit-testing.html)

### Packaging and running as standalone jar

```
lein do clean, ring uberjar
java -jar target/server.jar
```

### Packaging as war

`lein ring uberwar`

## License

Copyright © 2018 Brendan Boesen

-------------------------------

# Future Expansion
* Expand API to allow for:
  * Multiple start and/or end nodes.
  * Node identity by query (eg: by 'name').
* Include additional data from the database in the nodes. The existing limited set is purely for demonstration purposes.
* Examine loading Neo4J using a cypher script as opposed to a set of REST-API calls. It's currently too slow to load (about 1 hour on my MacBook Air).
* Consider using streaming for reading form the MySQL database. (This would be useful for the in-memory graph but not so much for Neo4J.)
* Add a UI to allow simple browsing:
  * Find node by id, data
  * Explore a node's relationships
  * Display a (limited depth) sub-graph from a node
  * etc.
* Explore using Datomic as a persistent datastore.

