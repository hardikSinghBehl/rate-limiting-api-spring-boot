## Rate limiting APIs using Token Bucket Algorithm
##### A reference proof-of-concept that leverages [Bucket4j](https://github.com/bucket4j/bucket4j) along with Redis Cache and Spring Security filters to implement Rate limiting on private API endpoints. 
##### 🛠 upgraded to Spring Boot 3 and Spring Security 6 🛠 

### Key Components
* [RedisConfiguration.java](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/configuration/RedisConfiguration.java)
* [RateLimitingService.java](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/service/RateLimitingService.java)
* [RateLimitFilter.java](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/filter/RateLimitFilter.java)
* [BypassRateLimit.java](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/configuration/BypassRateLimit.java)
* [Flyway Migration Scripts](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/resources/db/migration)

### Application Flow
* During the initial launch of the application, [Database tables](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/documentation/database_diagram.png) are created and populated with data using Flyway migration scripts. Specifically, the [plans](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/resources/db/migration/V002__adding_plans.sql) table is populated with predefined plans, each assigned a specific `limit_per_hour` value.

    | Name          | Limit per Hour |
    |---------------|----------------|
    | FREE          | 20             |
    | BUSINESS      | 40             |
    | PROFESSIONAL  | 100            |
  
* When a user account is created, the specified plan-id sent as part of the request is linked to the user's record. This plan dictates the rate limit configurations applicable to the user.
* When a user invokes a private API endpoint using a valid JWT token recieved post successful authentication, the application enforces rate limit based on the user's chosen plan. The rate limit enforcement occurs within the [RateLimitFilter](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/filter/RateLimitFilter.java), where the current configuration is managed by [RateLimitingService](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/service/RateLimitingService.java).
* Upon initial API invocation, the RateLimitingService fetches the user's plan details from the datasource, storing them in the cache for efficient retrieval on subsequent requests. This data stored in the form of a Bucket, is used to implement Token Bucket Algorithm using [Bucket4j](https://github.com/bucket4j/bucket4j).
* When the rate limit assigned gets exhausted for a user, the below API response is sent back to the client  
    ```
    {
      "Status": "429 TOO_MANY_REQUESTS",
      "Description": "API request limit linked to your current plan has been exhausted."
    }
    ```
* The current user plan can be updated, which removes the previous rate limit configuration stored in the cache. The private API endpoint to update plan has been configured to bypass rate limit checks using [@BypassRateLimit](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/configuration/BypassRateLimit.java), allowing access via a valid JWT token even when the current rate limit is exhausted.

### Rate Limit Headers
After evaluation of incoming HTTP requests against the user's rate limit, the [RateLimitFilter](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/filter/RateLimitFilter.java) includes additional HTTP headers in the response to provide more information. These headers are useful for client applications to understand the rate limit status and adjust their behavior accordingly to handle rate limit violations gracefully.

| Header Name                    | Description                                                                                                         |
|--------------------------------|---------------------------------------------------------------------------------------------------------------------|
| X-Rate-Limit-Remaining         | Indicates the number of remaining tokens available in the user's rate limit bucket after processing the request.   |
| X-Rate-Limit-Retry-After-Seconds | Specifies the wait period in seconds before the user can retry making requests, in case they exceed their rate limit. |

### Bypass Rate limit Enforcement
Bypassing rate limit enforcement for specific private API endpoints can be achieved by annotating the corresponding controller method(s) with the `@BypassRateLimit` annotation. When applied, requests to that method are not subjected to rate limiting by the [RateLimitFilter.java](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/filter/RateLimitFilter.java) and allowed regardless of the user's current rate limit plan.

The below private API endpoint to update a user's current plan is annotated with `@BypassRateLimit` to ensure requests to update to a new plan are not restricted by the user's rate limit.

```java
@BypassRateLimit
@PutMapping(value = "/api/v1/plan")
public ResponseEntity<HttpStatus> update(@RequestBody PlanUpdationRequest planUpdationRequest) {
    planService.update(planUpdationRequest);
    return ResponseEntity.status(HttpStatus.OK).build();
}
```

### Security Filters

All requests to private API endpoints are intercepted by the [JwtAuthenticationFilter](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/filter/JwtAuthenticationFilter.java). This filter holds the responsibility for verifying the signature of the incoming access token and populating the security context. Only when the access token's signature is validated successfully, does the request reach [RateLimitFilter](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/filter/RateLimitFilter.java) which enforces the rate limit for the user accordingly.

Both the custom filters are added to the Spring Security filter chain and configured in the [SecurityConfiguration](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/configuration/SecurityConfiguration.java).

Any API that needs to be made public can be configured in the active `.yml` file, the values are mapped to [ApiPathExclusionConfigurationProperties](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/configuration/ApiPathExclusionConfigurationProperties.java) and referenced by the application. Requests to the configured API paths are not evaluated by either of the filters with the logic being governed by [ApiEndpointSecurityInspector](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/main/java/com/behl/overseer/utility/ApiEndpointSecurityInspector.java).

Below is a sample snippet declaring public API endpoints in `application.yml` file.

```yaml
com:
  behl:
    overseer:
      unsecured:
        api-path:
          swagger-v3: true
          post:
            - /api/v1/user
            - /api/v1/auth/login
          get:
            - /api/v1/plan 
```

---
### Testing

[Testcontainers](https://github.com/testcontainers/testcontainers-java) have been utilized to effectively test the core functionality of Rate limiting in the application.

The below two essential tests can be examined to gain insight into the functionality and behavior of the rate limiting feature employed in the application:

* [RateLimitingServiceIT](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/test/java/com/behl/overseer/service/RateLimitingServiceIT.java)
* [JokeControllerIT](https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/blob/main/src/test/java/com/behl/overseer/controller/JokeControllerIT.java)

To run the entire Unit test and Integration test classes, the below commands can be executed respectively.

```bash
mvn test
```
```bash
mvn integration-test
```

---
### Local Setup
The below given commands can be executed in the project's base directory to build an image and start required container(s). Docker compose will initiate a MySQL and Redis container as well, with the backend swagger-ui accessible at `http://localhost:8080/swagger-ui.html`
```bash
sudo docker-compose build
```
```bash
sudo docker-compose up -d
```

---
### Visual Walkthrough

https://github.com/hardikSinghBehl/rate-limiting-api-spring-boot/assets/69693621/8a14800f-1fed-4ad7-8606-d8015d7f66a1

