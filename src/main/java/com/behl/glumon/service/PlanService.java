package com.behl.glumon.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.behl.glumon.dto.PlanResponseDto;
import com.behl.glumon.repository.PlanRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    public ResponseEntity<List<PlanResponseDto>> allPlansInSystemRetreivalHandler() {
        return ResponseEntity
                .ok(planRepository
                        .findAll().parallelStream().map(plan -> PlanResponseDto.builder().id(plan.getId())
                                .name(plan.getName()).limitPerHour(plan.getLimitPerHour()).build())
                        .collect(Collectors.toList()));
    }

}
