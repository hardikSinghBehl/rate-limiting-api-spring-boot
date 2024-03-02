package com.behl.overseer.filter;

import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.behl.overseer.dto.ExceptionResponseDto;
import com.behl.overseer.service.RateLimitingService;
import com.behl.overseer.utility.ApiEndpointSecurityInspector;
import com.behl.overseer.utility.AuthenticatedUserIdProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;
	private final RateLimitingService rateLimitingService;
	private final AuthenticatedUserIdProvider authenticatedUserIdProvider;
	private final ApiEndpointSecurityInspector apiEndpointSecurityInspector;
	
	private static final String RATE_LIMIT_ERROR = "API request limit linked to your current plan has been exhausted.";

	@Override
	@SneakyThrows
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
		final var unsecuredApiBeingInvoked = apiEndpointSecurityInspector.isUnsecureRequest(request);

		if (Boolean.FALSE.equals(unsecuredApiBeingInvoked)) {
			final var userId = authenticatedUserIdProvider.getUserId();
			final var bucket = rateLimitingService.getBucket(userId);
			final var consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);
			final var isConsumptionPassed = consumptionProbe.isConsumed();
			
			if (Boolean.FALSE.equals(isConsumptionPassed)) {
				final var httpStatus = HttpStatus.TOO_MANY_REQUESTS;
	            response.setStatus(httpStatus.value());

				final var waitPeriod = TimeUnit.NANOSECONDS.toSeconds(consumptionProbe.getNanosToWaitForRefill());
				response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitPeriod));

				final var exceptionResponse = new ExceptionResponseDto<String>();
				exceptionResponse.setStatus(httpStatus.toString());
				exceptionResponse.setDescription(RATE_LIMIT_ERROR);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				final var errorResponse = objectMapper.writeValueAsString(exceptionResponse);
				response.getWriter().write(errorResponse);
				return;
			}
			
			final var remainingTokens = consumptionProbe.getRemainingTokens();
			response.setHeader("X-Rate-Limit-Remaining", String.valueOf(remainingTokens));

		}
		filterChain.doFilter(request, response);
	}

}