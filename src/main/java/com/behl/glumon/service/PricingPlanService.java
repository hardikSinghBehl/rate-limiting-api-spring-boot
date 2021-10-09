package com.behl.glumon.service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.behl.glumon.repository.UserPlanMappingRepository;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PricingPlanService {

    private final Map<UUID, Bucket> bucketCache = new ConcurrentHashMap<UUID, Bucket>();
    private final UserPlanMappingRepository userPlanMappingRepository;

    public Bucket resolveBucket(UUID userId) {
        return bucketCache.computeIfAbsent(userId, this::newBucket);
    }

    private Bucket newBucket(UUID userId) {
        final var plan = userPlanMappingRepository.findByUserIdAndIsActive(userId, true).get().getPlan();
        final Integer limitPerHour = plan.getLimitPerHour();
        return Bucket4j.builder()
                .addLimit(Bandwidth.classic(limitPerHour, Refill.intervally(limitPerHour, Duration.ofHours(1))))
                .build();
    }
}