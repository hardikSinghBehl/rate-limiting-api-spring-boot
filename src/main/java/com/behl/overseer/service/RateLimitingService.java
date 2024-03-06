package com.behl.overseer.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.behl.overseer.repository.UserPlanMappingRepository;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimitingService {

	private final ProxyManager<UUID> proxyManager;
	private final UserPlanMappingRepository userPlanMappingRepository;

	/**
	 * Retrieves the stored rate-limiting bucket for the specified user. If no
	 * bucket is found for the user, a new one is created and stored in the
	 * provisioned cache based on the user's current subscription plan.
	 *
	 * @param userId unique identifier of the user.
	 * @return The rate-limiting {@link Bucket} associated with the user.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 */
	public Bucket getBucket(@NonNull final UUID userId) {
		return proxyManager.builder().build(userId, () -> createBucketConfiguration(userId));
	}

	/**
	 * Resets the rate limiting for the specified user-id.
	 *
	 * @param userId unique identifier of the user.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 */
	public void reset(@NonNull final UUID userId) {
		proxyManager.removeProxy(userId);
	}

	/**
	 * Constructs an instance of {@link BucketConfiguration} corresponding to the
	 * user's active plan which enforce the allowed rate-limit of API invocation.
	 *
	 * @param userId The unique identifier of the user.
	 * @return The bucket configuration for rate limiting based on the user's active plan.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 */
	private BucketConfiguration createBucketConfiguration(@NonNull final UUID userId) {
		final var userPlanMapping = userPlanMappingRepository.getActivePlan(userId);
		final var limitPerHour = userPlanMapping.getPlan().getLimitPerHour();
		return BucketConfiguration.builder()
				.addLimit(limit -> limit.capacity(limitPerHour).refillIntervally(limitPerHour, Duration.ofHours(1)))
				.build();
	}

}