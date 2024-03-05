package com.behl.overseer.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties controlling token generation, validation, and
 * expiration within the application. The configured values are referenced by
 * the application when generating/validating JWT tokens.
 * 
 * @see com.behl.overseer.utility.JwtUtility
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "com.behl.overseer.token")
public class TokenConfigurationProperties {
		
	/**
	 * The symmetric secret-key used for both signing and verifying the signature of
	 * received access token(s) to ensure authenticity.
	 * The configured value must be Base64 encoded.
	 */
	@NotBlank
	@Pattern(regexp = "^[a-zA-Z0-9+/]*={0,2}$", message = "Secret key must be Base64 encoded.")
	private String secretKey;

	/**
	 * The validity period of JWT access token(s) in minutes, post which the token
	 * expires and can no longer be used for authentication.
	 */
	@NotNull
	@Positive
	private Integer validity;

}