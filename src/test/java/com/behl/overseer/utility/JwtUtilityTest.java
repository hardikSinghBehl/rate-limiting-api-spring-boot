package com.behl.overseer.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.behl.overseer.configuration.TokenConfigurationProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

class JwtUtilityTest {

	private static final String JWT_STRUCTURE_REGEX = "^[^.]+\\.[^.]+\\.[^.]+$";

	private final TokenConfigurationProperties tokenConfigurationProperties = mock(TokenConfigurationProperties.class);
	private final static String issuer = "unit-test-issuer";
	private final JwtUtility jwtUtility = new JwtUtility(issuer, tokenConfigurationProperties);

	@Test
	void shouldGenerateValidAccessTokenForUserEntityWithRequiredClaims() {
		// Prepare test user id
		final var userId = UUID.randomUUID();

		// configure token configuration
		final var accessTokenValidity = 1;
		final var secretKey = Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded());
		when(tokenConfigurationProperties.getSecretKey()).thenReturn(secretKey);
		when(tokenConfigurationProperties.getValidity()).thenReturn(accessTokenValidity);

		// Generate access token for user entity
		final var accessToken = jwtUtility.generateAccessToken(userId);

		// Validate the generated access token and verify mock interactions
		assertThat(accessToken).isNotBlank().matches(JWT_STRUCTURE_REGEX);
		verify(tokenConfigurationProperties).getSecretKey();
		verify(tokenConfigurationProperties).getValidity();

		// Extract user-id from generated access token
		final var extractedUserId = jwtUtility.getUserId(accessToken);

		// Assert validity of extracted user ID
		assertThat(extractedUserId).isNotNull().isInstanceOf(UUID.class).isEqualTo(userId);
	}

	@Test
	void shouldThrowIllegalArgumentExceptionForNullArguments() {
		assertThrows(IllegalArgumentException.class, () -> jwtUtility.getUserId(null));
		assertThrows(IllegalArgumentException.class, () -> jwtUtility.generateAccessToken(null));
	}

}