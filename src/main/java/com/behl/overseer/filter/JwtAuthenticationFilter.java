package com.behl.overseer.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.behl.overseer.dto.ExceptionResponseDto;
import com.behl.overseer.utility.ApiEndpointSecurityInspector;
import com.behl.overseer.utility.JwtUtility;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * JwtAuthenticationFilter is a custom filter registered with the spring
 * security filter chain and works in conjunction with the security
 * configuration, as defined in {@link com.behl.overseer.configuration.SecurityConfiguration}. 
 * 
 * It is responsible for verifying the authenticity of incoming HTTP requests to
 * secured API endpoints by examining JWT token in the request header, verifying 
 * it's signature, expiration.
 * If authentication is successful, the filter populates the security context with
 * the user's unique identifier which can be referenced by the application later.
 * 
 * This filter is only executed when a secure API endpoint in invoked, and is skipped
 * if the incoming request is destined to a non-secured public API endpoint.
 *
 * @see com.behl.overseer.configuration.SecurityConfiguration
 * @see com.behl.overseer.utility.ApiEndpointSecurityInspector
 * @see com.behl.overseer.utility.JwtUtility
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;
	private final JwtUtility jwtUtility;
	private final ApiEndpointSecurityInspector apiEndpointSecurityInspector;
	
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String MISSING_TOKEN_ERROR_MESSAGE = "Authentication failure: Token missing, invalid or expired";

	@Override
	@SneakyThrows
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
		final var unsecuredApiBeingInvoked = apiEndpointSecurityInspector.isUnsecureRequest(request);
		
		if (Boolean.FALSE.equals(unsecuredApiBeingInvoked)) {
			final var authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
	
			if (StringUtils.isNotEmpty(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX) ) {
				final var token = authorizationHeader.replace(BEARER_PREFIX, StringUtils.EMPTY);
				
				final var userId = jwtUtility.getUserId(token);
				final var authentication = new UsernamePasswordAuthenticationToken(userId, null, null);
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				setAuthErrorDetails(response);
				return;
			}
		}
		filterChain.doFilter(request, response);
	}
	
	/**
	 * Sets the authentication error details in the HTTP response. 
	 * 
	 * @param response instance of HttpServletResponse to which error response will be set.
	 */
	@SneakyThrows
	private void setAuthErrorDetails(HttpServletResponse response) {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		final var errorResponse = prepareErrorResponseBody();
		response.getWriter().write(errorResponse);
	}
	
	/**
	 * Returns a JSON representation of the invalid token error response body.
	 */
	@SneakyThrows
	private String prepareErrorResponseBody() {
		final var exceptionResponse = new ExceptionResponseDto<String>();
		exceptionResponse.setStatus(HttpStatus.UNAUTHORIZED.toString());
		exceptionResponse.setDescription(MISSING_TOKEN_ERROR_MESSAGE);
		return objectMapper.writeValueAsString(exceptionResponse);
	}

}