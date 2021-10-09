package com.behl.glumon.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.behl.glumon.repository.UserRepository;
import com.behl.glumon.service.RateLimitingService;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final RateLimitingService rateLimitingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            final var loggedInUser = userRepository.findByEmailId(authentication.getName()).get();
            final Bucket tokenBucket = rateLimitingService.resolveBucket(loggedInUser.getId());
            final ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);

            if (!probe.isConsumed()) {
                response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                        "Request limit linked to your current plan has been exhausted");
            }
        }
        filterChain.doFilter(request, response);
    }

}
