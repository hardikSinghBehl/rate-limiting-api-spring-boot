package com.behl.overseer.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.behl.overseer.filter.JwtAuthenticationFilter;
import com.behl.overseer.filter.RateLimitFilter;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Configuration class responsible for defining and configuring the security
 * settings for the application. It sets up the following components and
 * features:
 * <ul>
 *   <li>Configuration of non-secured public API endpoints.</li>
 *   <li>Integration of custom JWT Auth filter into the security filter chain to
 *       ensure that all requests to private API endpoints pass through the filter
 *       for authentication verification.</li>
 *   <li>Integration of custom Rate limiting filter into the security filter chain
 *       to ensure private API endpoints are invoked by an authenticated user
 *       within their corresponding {@link com.behl.overseer.entity.Plan} </li>
 * </ul>
 *
 * @see com.behl.overseer.configuration.ApiPathExclusionConfigurationProperties
 * @see com.behl.overseer.filter.JwtAuthenticationFilter
 * @see com.behl.overseer.filter.RateLimitFilter
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ApiPathExclusionConfigurationProperties.class)
public class SecurityConfiguration {

	private final RateLimitFilter rateLimitFilter;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ApiPathExclusionConfigurationProperties apiPathExclusionConfigurationProperties;
	
	private static final List<String> SWAGGER_V3_PATHS = List.of("/swagger-ui**/**", "/v3/api-docs**/**");

	@Bean
	@SneakyThrows
	public SecurityFilterChain configure(final HttpSecurity http)  {
		final var unsecuredGetEndpoints = Optional.ofNullable(apiPathExclusionConfigurationProperties.getGet()).orElseGet(ArrayList::new);
		final var unsecuredPostEndpoints = Optional.ofNullable(apiPathExclusionConfigurationProperties.getPost()).orElseGet(ArrayList::new);
		
		if (Boolean.TRUE.equals(apiPathExclusionConfigurationProperties.isSwaggerV3())) {
			unsecuredGetEndpoints.addAll(SWAGGER_V3_PATHS);
			apiPathExclusionConfigurationProperties.setGet(unsecuredGetEndpoints);
		}
		
		http
			.cors(corsConfigurer -> corsConfigurer.disable())
			.csrf(csrfConfigurer -> csrfConfigurer.disable())
			.sessionManagement(sessionConfigurer -> sessionConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authManager -> {
					authManager
						.requestMatchers(HttpMethod.GET, unsecuredGetEndpoints.toArray(String[]::new)).permitAll()
						.requestMatchers(HttpMethod.POST, unsecuredPostEndpoints.toArray(String[]::new)).permitAll()
					.anyRequest().authenticated();
				})
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);

		return http.build();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}