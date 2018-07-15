# dana

Dana is a web application to allow Dana to explore the Paradise Papers dataset.

## Setup

You will need a local mysql database loaded with the Paradise Papers MySQL dump. (Contact author for availability.)

### OSX

> Assumes you have `brew` installed.

Install mysql:
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
> ** No
> * Change the password for root ?
> ** Yes
> ** Set it to 'password'
> * Remove anonymous users?
> ** Yes
> * Disallow root login remotely?
> ** Yes
> * Remove test database and access to it?
> ** Yes
> * Reload privilege tables now?
> ** Yes

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

Install Neo4J:
```
brew install neo4j
```

Login to the Neo4J server (http://localhost:7474/browser/) using usename/password `neo4j` and change the password to `password`.

## Usage

### Run the application locally

`lein ring server`

This will open up a web-browser showing a 'swagger' view of the API. Expand the shortest-paths endpoint and enter two node ids, eg: XXX and XXX, then hit 'Try it out!'.

### Run the tests

`lein test`

or

`lein test-refresh`

to run them continually during development.

### Packaging and running as standalone jar

```
lein do clean, ring uberjar
java -jar target/server.jar
```

### Packaging as war

`lein ring uberwar`

## License

Copyright © 2018 Brendan Boesen

# Useful Neo4J Queries

* Count all nodes: `match (n) return count(n)`
* Count all relationships `match (n)-[r]->() return count(r)`

* Delete all relationships: `match (n)-[r]->() delete r`
* Delete all nodes (after deleting relationships): `match (n) delete n`
