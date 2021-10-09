package com.behl.glumon.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.behl.glumon.dto.PlanResponseDto;
import com.behl.glumon.dto.UserPlanUpdationRequestDto;
import com.behl.glumon.security.utility.JwtUtils;
import com.behl.glumon.service.PlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class PlanController {

    private final PlanService planService;
    private final JwtUtils jwtUtils;

    @GetMapping(value = "/plans", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "Returns all plan details in the system")
    public ResponseEntity<List<PlanResponseDto>> allPlansInSystemRetreivalHandler() {
        return planService.allPlansInSystemRetreivalHandler();
    }

    @PutMapping(value = "/plan")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "Updates logged in users plan")
    public ResponseEntity<?> userPlanUpdationHandler(
            @Parameter(hidden = true) @RequestHeader(name = "Authorization", required = true) final String header,
            @RequestBody(required = true) final UserPlanUpdationRequestDto userPlanUpdationRequestDto) {
        return planService.updatePlan(jwtUtils.extractUserId(header), userPlanUpdationRequestDto);
    }

}
