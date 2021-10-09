package com.behl.glumon.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.behl.glumon.dto.PlanResponseDto;
import com.behl.glumon.service.PlanService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping(value = "/plans", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "Returns all plan details in the system")
    public ResponseEntity<List<PlanResponseDto>> allPlansInSystemRetreivalHandler() {
        return planService.allPlansInSystemRetreivalHandler();
    }

}
