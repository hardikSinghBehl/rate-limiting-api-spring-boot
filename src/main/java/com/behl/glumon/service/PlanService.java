package com.behl.glumon.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.behl.glumon.dto.PlanResponseDto;
import com.behl.glumon.dto.UserPlanUpdationRequestDto;
import com.behl.glumon.entity.UserPlanMapping;
import com.behl.glumon.repository.PlanRepository;
import com.behl.glumon.repository.UserPlanMappingRepository;
import com.behl.glumon.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final UserPlanMappingRepository userPlanMappingRepository;
    private final RateLimitingService rateLimitingService;

    public ResponseEntity<List<PlanResponseDto>> allPlansInSystemRetreivalHandler() {
        return ResponseEntity
                .ok(planRepository
                        .findAll().parallelStream().map(plan -> PlanResponseDto.builder().id(plan.getId())
                                .name(plan.getName()).limitPerHour(plan.getLimitPerHour()).build())
                        .collect(Collectors.toList()));
    }

    @Transactional
    public ResponseEntity<?> updatePlan(final UUID loggedInUser,
            final UserPlanUpdationRequestDto userPlanUpdationRequestDto) {
        final var user = userRepository.findById(loggedInUser).get();
        final var plan = planRepository.findById(userPlanUpdationRequestDto.getPlanId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid plan-id provided"));

        if (userPlanMappingRepository.existsByUserAndPlan(user, plan))
            return ResponseEntity.ok().build();

        userPlanMappingRepository.invalidatePreviousPlans(loggedInUser);

        final var userPlanMapping = new UserPlanMapping();
        userPlanMapping.setUser(user);
        userPlanMapping.setPlan(plan);
        userPlanMapping.setIsActive(true);
        userPlanMappingRepository.save(userPlanMapping);

        rateLimitingService.deleteIfExists(user.getId());
        return ResponseEntity.ok().build();
    }

}
