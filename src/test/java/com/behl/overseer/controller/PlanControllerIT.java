package com.behl.overseer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class PlanControllerIT {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private PlanService planService;
	
	@Test
	@SneakyThrows
	void shouldFetchAvailablePlansFromSystem() {
		// execute API request
		final var apiPath = "/api/v1/plan";
		mockMvc.perform(get(apiPath))
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(jsonPath("$.[*].Id").exists())
			.andExpect(jsonPath("$.[*].Name").exists())
			.andExpect(jsonPath("$.[*].LimitPerHour").exists());
	}
	
	@Test
	@SneakyThrows
	void shouldUpdateUserPlanSuccessfully() {
		// fetch available plans from system
		final var plans = planService.retrieve();
		final var userPlan = plans.get(0);
		final var planToUpdate = plans.get(1);
		
		// create user and get valid user access token
		final var accessToken = createUserAndGenerateAccessToken(userPlan.getId());
		
		// prepare API request body to update user plan
		final var requestBody = String.format("""
		{
			"PlanId"   : "%s"
		}
		""", planToUpdate.getId());
		
		// execute API request
		final var apiPath = "/api/v1/plan";
		mockMvc.perform(put(apiPath)
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestBody)
			.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk());
	}
	
	@SneakyThrows
	private String createUserAndGenerateAccessToken(UUID planId) {
		// prepare API request body to create user
		final var emailId = RandomString.make() + "@domain.it";
		final var password = RandomString.make();
		final var userCreationRequestBody = String.format("""
		{
			"EmailId"  : "%s",
			"Password" : "%s",
			"PlanId"   : "%s"
		}
		""", emailId, password, planId);

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