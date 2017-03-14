## Spring Cloud Feign Logging - Not Working

This project is solely provided to demonstrate that the Spring Cloud Feign Implementation is not logging as described [in the docs](http://projects.spring.io/spring-cloud/spring-cloud.html#_feign_logging) or I have the configuration wrong.

#### Steps to Reproduce:

1. Clone this Repo.
2. Clone JoshLong's [bootiful-microservices-config Repo](https://github.com/joshlong/bootiful-microservices-config).
3. Update the `bootiful-microservices-config` repo with the config change for logging. 

	* Edit `bootiful-microservices-config/reservation-client.properties` and add the following line:
	
		```
		logging.level.com.example.ReservationReader=BASIC
		```
	
	* Commit the file update with `git commit -a -m "update for logging"`.
4. Setup the ConfigService to point to `bootiful-microservices-config`. Edit `config-service/src/main/resources/application.properties`. Set the `spring.cloud.config.server.git.uri` to the directory where you cloned `bootiful-microservices-config`.

5. Startup the Config Service.

	```
	cd config-service
	./gradlew bootRun
	```
	And leave it running throughout in it's own terminal.
	
6. Verify the Config Service has the update for logging visible when the Reservation Client comes up. Do the following GET request with `httpie` or `curl`:

	```
	$ http localhost:8888/reservation-client/default
	
	HTTP/1.1 200 
	Content-Type: application/json;charset=UTF-8
	Date: Tue, 14 Mar 2017 10:17:36 GMT
	Transfer-Encoding: chunked
	X-Application-Context: application:8888
	
	{
	    "label": "master",
	    "name": "reservation-client",
	    "profiles": [
	        "default"
	    ],
	    "propertySources": [
	        {
	            "name": "/Users/me/repos/spring-cloud-feign-logging-bug/bootiful-microservices-config/reservation-client.properties",
	            "source": {
	                "logging.level.com.example.ReservationReader": "BASIC",
	                "security.oauth2.resource.userInfoUri": "http://localhost:9191/uaa/user",
	                "server.port": "${PORT:9999}",
	                "spring.cloud.stream.bindings.output.destination": "reservations"
	            }
	        },
	        {
	            "name": "/Users/me/repos/spring-cloud-feign-logging-bug/bootiful-microservices-config/application.properties",
	            "source": {
	                "debug": "true",
	                "endpoints.jmx.enabled": "false",
	                "endpoints.shutdown.enabled": "true",
	                "info.id": "${spring.application.name}",
	                "logging.level.com.netflix.discovery": "OFF",
	                "logging.level.com.netflix.eureka": "OFF",
	                "logging.level.org.springframework.security": "DEBUG",
	                "management.security.enabled": "false",
	                "spring.jmx.enabled": "false",
	                "spring.jpa.generate-ddl": "true",
	                "spring.sleuth.log.json.enabled": "true",
	                "spring.sleuth.sampler.percentage": "1.0"
	            }
	        }
	    ],
	    "state": null,
	    "version": "11c31b1b7680c889ff5da80741f74dac0c60ebcc"
	}
	```
	
	Under the one of the `PropertySources` array elements, you need to see a `source` with `"logging.level.com.example.ReservationReader": "BASIC"` to ensure that the Config Service is serving your changes.
	
7. Startup the Eureka Service:

	```
	cd eureka-service
	./gradlew bootRun
	```
	And leave it running throughout in it's own terminal.
	
8. Startup the Reservation Service:

	```
	cd reservation-service
	./gradlew bootRun
	```
	And leave it running throughout in it's own terminal.
	
9. Startup the Reservation Client:

	```
	cd reservation-client
	./gradlew bootRun
	```
	And leave it running throughout in it's own terminal.
	
10. Make a test call to the Reservation Client, which is running on port 9999. Do the following GET request with `httpie` or `curl`:
 
	```
	$ http localhost:9999/reservations/names
	
	HTTP/1.1 200 
	Content-Type: application/json;charset=UTF-8
	Date: Tue, 14 Mar 2017 10:44:03 GMT
	Transfer-Encoding: chunked
	X-Application-Context: reservation-client:9999
	
	[
	    "John",
	    "Jack",
	    "Jimmmy",
	    "Joe"
	]
	```

11. Check the window with the Reservation Client. Observe that there is no log output from Feign. According to the docs, there should be.