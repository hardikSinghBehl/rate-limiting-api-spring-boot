package com.behl.overseer.configuration;

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
import com.behl.overseer.utility.ApiEndpointSecurityInspector;

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
 * @see com.behl.overseer.filter.JwtAuthenticationFilter
 * @see com.behl.overseer.filter.RateLimitFilter
 * @see com.behl.overseer.utility.ApiEndpointSecurityInspector
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

	private final RateLimitFilter rateLimitFilter;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ApiEndpointSecurityInspector apiEndpointSecurityInspector;
	
	@Bean
	@SneakyThrows
	public SecurityFilterChain configure(final HttpSecurity http)  {
		http
			.cors(corsConfigurer -> corsConfigurer.disable())
			.csrf(csrfConfigurer -> csrfConfigurer.disable())
			.sessionManagement(sessionConfigurer -> sessionConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authManager -> {
					authManager
						.requestMatchers(HttpMethod.GET, apiEndpointSecurityInspector.getPublicGetEndpoints().toArray(String[]::new)).permitAll()
						.requestMatchers(HttpMethod.POST, apiEndpointSecurityInspector.getPublicPostEndpoints().toArray(String[]::new)).permitAll()
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