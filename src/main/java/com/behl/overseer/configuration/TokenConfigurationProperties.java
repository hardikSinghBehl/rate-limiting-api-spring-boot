package com.behl.overseer.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties controlling token generation, validation, and
 * expiration within the application.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "com.behl.overseer.token")
public class TokenConfigurationProperties {
		
	@Valid
	private AccessToken accessToken = new AccessToken();

	@Getter
	@Setter
	public class AccessToken {
		
		/**
		 * The symmetric secret-key used for both signing and verifying the signature of
		 * received access token(s) to ensure authenticity.
		 * The configured value must be Base64 encoded
		 * 
		 * @see com.behl.overseer.utility.JwtUtility
		 */
		@NotBlank
		private String secretKey;

		/**
		 * The validity period of JWT access token(s) in minutes, post which the token
		 * expires and can no longer be used for authentication.
		 * 
		 * @see com.behl.overseer.utility.JwtUtility
		 */
		@NotNull
		@Positive
		private Integer validity;
		
	}

}