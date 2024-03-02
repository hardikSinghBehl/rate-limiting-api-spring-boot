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

	public Bucket getBucket(@NonNull final UUID userId) {
		return proxyManager.builder().build(userId, () -> createBucketConfiguration(userId));
	}

	public void reset(@NonNull final UUID userId) {
		proxyManager.removeProxy(userId);
	}

	private BucketConfiguration createBucketConfiguration(@NonNull final UUID userId) {
		final var userPlanMapping = userPlanMappingRepository.findByUserIdAndIsActive(userId, Boolean.TRUE);
		final var limitPerHour = userPlanMapping.getPlan().getLimitPerHour();
		return BucketConfiguration.builder()
				.addLimit(limit -> limit.capacity(limitPerHour).refillIntervally(limitPerHour, Duration.ofHours(1)))
				.build();
	}

}