package com.behl.overseer.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.behl.overseer.InitializeApplicationSecretKey;
import com.behl.overseer.InitializeMysqlContainer;
import com.behl.overseer.InitializeRedisContainer;
import com.behl.overseer.configuration.PublicEndpoint;

import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;

@RestController
class TestController {

	@PublicEndpoint
	@GetMapping(value = "/api/v1/public-endpoint")
	public ResponseEntity<HttpStatus> publicEndpoint() {
		return ResponseEntity.ok().build();
	}

	@GetMapping(value = "/api/v1/private-endpoint")
	public ResponseEntity<HttpStatus> privateEndpoint() {
		return ResponseEntity.ok().build();
	}
	
}

@SpringBootTest
@AutoConfigureMockMvc
@InitializeRedisContainer
@InitializeMysqlContainer
@InitializeApplicationSecretKey
class ApiEndpointSecurityInspectorIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ApiEndpointSecurityInspector apiEndpointSecurityInspector;
	
	@Test
	void shouldReturnTrueIfHttpRequestDirectedTowardsUnsecuredApiEndpoint() {
		// defining API path to invoke
		final var apiPath = "/api/v1/public-endpoint";

		// simulating incoming HTTP request
		final var httpRequest = mock(HttpServletRequest.class);
		when(httpRequest.getMethod()).thenReturn(HttpMethod.GET.name());
		when(httpRequest.getRequestURI()).thenReturn(apiPath);

		// invoke method under test
		final var result = apiEndpointSecurityInspector.isUnsecureRequest(httpRequest);

		// assert response
		assertThat(result).isTrue();
	}
	
	@Test
	void shouldReturnFalseIfHttpRequestDirectedTowardsSecuredApiEndpoint() {
		// defining API path to invoke
		final var apiPath = "/api/v1/private-endpoint";

		// simulating incoming HTTP request
		final var httpRequest = mock(HttpServletRequest.class);
		when(httpRequest.getMethod()).thenReturn(HttpMethod.GET.name());
		when(httpRequest.getRequestURI()).thenReturn(apiPath);

		// invoke method under test
		final var result = apiEndpointSecurityInspector.isUnsecureRequest(httpRequest);

		// assert response
		assertThat(result).isFalse();
	}

	@Test
	@SneakyThrows
	void publicEndpointShouldBeAccessibleWithoutAuthToken() {
		// invoke public-endpoint without auth token and assert response
		final var apiPath = "/api/v1/public-endpoint";
		mockMvc.perform(get(apiPath))
				.andExpect(status().isOk());
	}
	
	@Test
	@SneakyThrows
	void privateEndpointShouldBeInaccessibleWithoutAuthToken() {
		// invoke private-endpoint without auth token and assert response
		final var apiPath = "/api/v1/private-endpoint";
		mockMvc.perform(get(apiPath))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.Status").value(HttpStatus.UNAUTHORIZED.toString()))
				.andExpect(jsonPath("$.Description").value("Authentication failure: Token missing, invalid or expired"));
	}

}