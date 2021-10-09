## Rate limiting REST APIs using Spring-security filter and [Bucket4J](https://github.com/vladimir-bukhtoyarov/bucket4j)
#### [Deployed Application (Swagger-ui on heroku)](https://rate-limit-api-spring-boot.herokuapp.com/swagger-ui.html)
#### Inspired from: [Baeldung Article](https://www.baeldung.com/spring-bucket4j)
### Application flow
* There are 3 entities in this POC with the mentioned key columns
  * users
    * user_id (UUID)
    * email_id
    * password
  * plans
    * plan_id (UUID)
    * name
    * limit_per_hour 
  * user_plan_mappings
    * user_id
    * plan_id
    * is_active
* Three plans are inserted in the H2-in memory database on startup [PlanDataInitializer.class](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/glumon/bootstrap/PlanDataInitializer.java)
* A user_account (record in users table) is created and linked to the provided plan using the /sign-up API path
* We create a bucket using [Bucket4j](https://github.com/vladimir-bukhtoyarov/bucket4j) corresponding to the user_id and store it in an in-memory cache (ConcurrentHashMap used for demo purposes) when the user hits a private API using the JWT recieved after successfull login (user_id is encoded in the JWT)
* The above mentioned logic is implemented in the [RateLimitingService.class](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/glumon/service/RateLimitingService.java)
* We create a [RateLimitFilter](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/glumon/security/filter/RateLimitFilter.java) extending the OncePerRequestFilter.class and it to the spring-security filter chain
* We send an error response of HttpStatus.TOO_MANY_REQUESTS, if the user has exchausted the limit assigned to them per their configured plan
* Remove the <UUID, Bucket> mapping in the in-memory cache when the user updates their plan

### Sample Screen Recording (1 minute long)

https://user-images.githubusercontent.com/69693621/136645949-0a0866d2-c944-46b3-8bc7-1332382e2e85.mov

---

### Local Setup
* Install Java 17 (recommended to use [SdkMan](https://sdkman.io))

`sdk install java 17-open`
* Install Maven (recommended to use [SdkMan](https://sdkman.io))

`sdk install maven`

* Clone the repo and run the below command in core

`mvn clean install`

* To start the application, run any of the below 2 commands

`mvn spring-boot:run &`

`java -jar /target/rate-limiting-api-spring-boot-0.0.1-SNAPSHOT.jar &`

* Access the swagger-ui

`http://localhost:8080/swagger-ui.html`
