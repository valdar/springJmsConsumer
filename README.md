Spring Jms Consumer
===================
This is one of a set of 3 example projects (valdar/springRestServer , valdar/springRestClient) that I made for an interview.  
It is a simple JMS consumer with limited number of consumed message per second, packaged in a single jar and configurable by a property file in the same directory of the jar.  
The project is managed with maven.

To build the project:
```sh
mvn package
```

To run it:
```sh
mvn exec:exec
```

Whit this command it'll use a copy of the property file in the root of the project.  
Otherwise you can copy the jar file created in the target directory, create you own property config file, put all in the same direcorty and run it with:
```sh
java -jar springJmsConsumer.jar
```