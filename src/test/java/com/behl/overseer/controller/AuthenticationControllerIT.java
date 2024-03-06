package com.behl.overseer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
class AuthenticationControllerIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private PlanService planService;
	
	@Test
	@SneakyThrows
	void userCreationShouldFailForInvalidPlanId() {
		// construct invalid random plan-id
		final var planId = UUID.randomUUID();
		
		// prepare API request body to create user
		final var emailId = RandomString.make() + "@domain.it";
		final var requestBody = String.format("""
		{
			"EmailId"  : "%s",
			"Password" : "SomethingSecure",
			"PlanId"   : "%s"
		}
		""", emailId, planId);

		// execute API request
		final var apiPath = "/api/v1/user";
		mockMvc.perform(post(apiPath)
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestBody))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.Status").value(HttpStatus.NOT_FOUND.toString()))
			.andExpect(jsonPath("$.Description").value("No plan exists in the system with provided-id"));
	}
	
	@Test
	@SneakyThrows
	void shouldCreateUserEntityForValidUserCreationRequest() {
		// fetch plan from datasource
		final var plan = planService.retrieve().get(0);
		
		// prepare API request body to create user
		final var emailId = RandomString.make() + "@domain.it";
		final var requestBody = String.format("""
		{
			"EmailId"  : "%s",
			"Password" : "SomethingSecure",
			"PlanId"   : "%s"
		}
		""", emailId, plan.getId());

		// execute API request
		final var apiPath = "/api/v1/user";
		mockMvc.perform(post(apiPath)
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestBody))
			.andExpect(status().isCreated());
	}
	
	@Test
	@SneakyThrows
	void userCreationShouldThrowExceptionForDuplicateEmailId() {
		// fetch plan from datasource
		final var plan = planService.retrieve().get(0);
		
		// prepare API request body to create user
		final var emailId = RandomString.make() + "@domain.it";
		final var requestBody = String.format("""
		{
			"EmailId"  : "%s",
			"Password" : "SomethingSecure",
			"PlanId"   : "%s"
		}
		""", emailId, plan.getId());

		// execute API request and assert success in initial execution
		final var apiPath = "/api/v1/user";
		mockMvc.perform(post(apiPath)
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestBody))
			.andExpect(status().isCreated());
		
		// execute API request with same email-id again
		mockMvc.perform(post(apiPath)
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestBody))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.Status").value(HttpStatus.CONFLICT.toString()))
			.andExpect(jsonPath("$.Description").value("Account with provided email-id already exists"));
	}
	
	@Test
	@SneakyThrows
	void loginShouldReturnUnauthorizedAgainstUnregisteredEmailId() {
		// prepare API request body for login with random email-id
		final var emailId = RandomString.make() + "@domain.it";
		final var requestBody = String.format("""
		{
			"EmailId"  : "%s",
			"Password" : "SomethingSecure"
		}
		""", emailId);

		// execute API request
		final var apiPath = "/api/v1/auth/login";
		mockMvc.perform(post(apiPath)
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestBody))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.Status").value(HttpStatus.UNAUTHORIZED.toString()))
			.andExpect(jsonPath("$.Description").value("Invalid login credentials provided"));
	}
	
	@Test
	@SneakyThrows
	void loginShouldReturnUnauthorizedAgainstInvalidPassword() {
		// fetch plan from datasource
		final var plan = planService.retrieve().get(0);
		
		// prepare API request body to create user
		final var emailId = RandomString.make() + "@domain.it";
		final var userCreationRequestBody = String.format("""
		{
			"EmailId"  : "%s",
			"Password" : "SomethingSecure",
			"PlanId"   : "%s"
		}
		""", emailId, plan.getId());

		// execute API request to create user
		final var userCreationApiPath = "/api/v1/user";
		mockMvc.perform(post(userCreationApiPath)
			.contentType(MediaType.APPLICATION_JSON)
			.content(userCreationRequestBody))
			.andExpect(status().isCreated());
		
		// prepare API request body with invalid password
		final var password = RandomString.make();
		final var loginRequestBody = String.format("""
		{
			"EmailId"  : "%s",
			"Password" : "%s"
		}
		""", emailId, password);

		// execute API request for login
		final var loginApiPath = "/api/v1/auth/login";
		mockMvc.perform(post(loginApiPath)
			.contentType(MediaType.APPLICATION_JSON)
			.content(loginRequestBody))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.Status").value(HttpStatus.UNAUTHORIZED.toString()))
			.andExpect(jsonPath("$.Description").value("Invalid login credentials provided"));
	}
	
	@Test
	@SneakyThrows
	void shouldReturnAccessTokenForValidLoginRequest() {
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
		mockMvc.perform(post(loginApiPath)
			.contentType(MediaType.APPLICATION_JSON)
			.content(loginRequestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.AccessToken").exists());
	}

}