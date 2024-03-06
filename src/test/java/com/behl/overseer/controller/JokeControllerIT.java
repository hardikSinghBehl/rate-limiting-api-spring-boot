package com.behl.overseer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.behl.overseer.InitializeApplicationSecretKey;
import com.behl.overseer.InitializeMysqlContainer;
import com.behl.overseer.InitializeRedisContainer;
import com.behl.overseer.service.PlanService;

import lombok.SneakyThrows;
import net.bytebuddy.utility.RandomString;

@SpringBootTest
@AutoConfigureMockMvc
@InitializeRedisContainer
@InitializeMysqlContainer
@InitializeApplicationSecretKey
class JokeControllerIT {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private PlanService planService;
	
	@Test
	@SneakyThrows
	void generateJokeShouldThrowForbiddenForMissingAccessToken() {
		// invoke private joke endpoint without access token
		final var apiPath = "/api/v1/joke";
		mockMvc.perform(get(apiPath))
			.andExpect(status().isForbidden());
	}
	
	@Test
	@SneakyThrows
	void shouldGenerateJokeForValidAccessToken() {
		// get valid user access token
		final var accessToken = createUserAndGenerateAccessToken();
		
		// invoke private joke endpoint with token
		final var apiPath = "/api/v1/joke";
		mockMvc.perform(get(apiPath)
				.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.Joke").isNotEmpty())
				.andExpect(header().exists("X-Rate-Limit-Remaining"));
	}
	
	@Test
	@SneakyThrows
	void shouldThrowRateLimitErrorIfJokeEndpointIsSpammed() {
		// get valid user access token
		final var accessToken = createUserAndGenerateAccessToken();
		
		// invoke private joke endpoint intially with token
		final var apiPath = "/api/v1/joke";
		final var response = mockMvc.perform(get(apiPath)
				.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.Joke").isNotEmpty())
				.andExpect(header().exists("X-Rate-Limit-Remaining"))
				.andReturn();
		
		// retrieve rate limit remaining from response header
		final var headerResponse = response.getResponse().getHeader("X-Rate-Limit-Remaining");
		final var rateLimitRemaining = Integer.parseInt(headerResponse);
		
		// exhaust available rate limit
		for (int i = 0; i < rateLimitRemaining; i++) {
			mockMvc.perform(get(apiPath)
					.header("Authorization", "Bearer " + accessToken))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.Joke").isNotEmpty())
					.andExpect(header().exists("X-Rate-Limit-Remaining"));
		}
		
		// invoke endpoint after rate limit exhaustion
		mockMvc.perform(get(apiPath)
				.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.Status").value(HttpStatus.TOO_MANY_REQUESTS.toString()))
				.andExpect(jsonPath("$.Description").value("API request limit linked to your current plan has been exhausted."))
				.andExpect(header().exists("X-Rate-Limit-Retry-After-Seconds"));
	}
	
	@SneakyThrows
	private String createUserAndGenerateAccessToken() {
		// fetch plan from datasource
		final var plan = planService.retrieve().get(0);
		
		// prepare API request body to create user
		final var emailId = RandomString.make() + "@domain.it";
		final var password = RandomString.make();
		final var userCreationRequestBody = String.format("""
		{
			"EmailId"  : "%s",
			"Password" : "%s",
			"PlanId"   : "%s"
		}
		""", emailId, password, plan.getId());

		// execute API request to create user
		final var userCreationApiPath = "/api/v1/user";
		mockMvc.perform(post(userCreationApiPath)
			.contentType(MediaType.APPLICATION_JSON)
			.content(userCreationRequestBody))
			.andExpect(status().isCreated());
		
		// prepare API request body for login with valid credentials
		final var loginRequestBody = String.format("""
		{
			"EmailId"  : "%s",
			"Password" : "%s"
		}
		""", emailId, password);

		// execute API request for login
		final var loginApiPath = "/api/v1/auth/login";
		final var response = mockMvc.perform(post(loginApiPath)
			.contentType(MediaType.APPLICATION_JSON)
			.content(loginRequestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.AccessToken").exists())
			.andReturn();
		
	    final var jsonResponse = response.getResponse().getContentAsString();
	    final var jsonObject = new JSONObject(jsonResponse);
	    return jsonObject.getString("AccessToken");
	}
	
}
