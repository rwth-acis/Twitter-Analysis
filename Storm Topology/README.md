# Cloud-Based Near Real-Time Event Analysis Framework for Twitter
Cloud-Based Near Real-Time Event Analysis Framework for Twitter is an Apache Storm topology that provide tweets collection and analysis.

System requirements
-------------------

- Java OpenJDK 7
- maven         
- Apache Storm 
- Zookeeper   
- ZeroMQ     
- jzmq        
- MongoDB      


Topology Configuration Instructions
-----------------------------------

1.	Open Tool class in the Storm Project in IDEA IntelliJ or Eclipse
2.	Input the used IP address and port for MongoDB into ```MONGO_IP``` and ```MONGO_PORT``` variables, respectively
3.	Before you can use existing topologies you must enter your key for Twitter API. The Twitter API keys may be obtained on Twitter Developers web site. You cannot start several topologies with the same set of API keys simultaneously.
4.	If you want to create a new topology you need to create a new class in the project. Take an existing topology as an example and change there the following variables:

* ```String consumerKey``` – your consumer key to Twitter API
* ```String consumerSecret``` – your consumer secret to Twitter API
* ```String accessToken``` – an access Token to Twitter API
* ```String accessTokenSecret``` – an access token secret to Twitter API
* ```String[] keyWords``` – a set of keywords that are used as filter for Twitter Streaming API
* ```String dbName``` – a name of a database where the data will be stored
* ```String eventName``` – a name of an event that is going to monitore
* ```String description``` – a small description of an event

Topology Start Instructions
---------------------------

1.	Build the project from the project location by running:
  
  ```
  mvn package
  ```
2.	Start the topology in local mode on Apache Storm:
  
  ```
  <Apache_storm-location>/bin/storm jar target/<name_of_the_generated_jar_file>.jar <topology_name_in-project> <Visiable_in_apache_storm_UI_topology_name> -c nimbus.host=<nimbus_host>
  ```
  
  Local mode example:
  
  ```
  ~/apache-storm-0.9.2-incubating/bin/storm jar ~/apache-storm-0.9.2-incubating/examples/TwitterCounter/target/twitter-analysis-0.9.3-incubating-SNAPSHOT-jar-with-dependencies.jar storm.twitter.BoschTopology BoschTopology -c nimbus.host=localhost
  ```

