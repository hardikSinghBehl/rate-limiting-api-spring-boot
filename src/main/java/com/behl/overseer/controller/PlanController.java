package com.behl.overseer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.behl.overseer.dto.PlanResponseDto;
import com.behl.overseer.dto.PlanUpdationRequestDto;
import com.behl.overseer.service.PlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PlanController {

	private final PlanService planService;

	@GetMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<PlanResponseDto>> retrieve() {
		return ResponseEntity.ok(planService.retrieve());
	}

	@PutMapping(value = "/plan")
	public ResponseEntity<HttpStatus> update(@Valid @RequestBody final PlanUpdationRequestDto planUpdationRequest) {
		planService.update(planUpdationRequest);
		return ResponseEntity.ok().build();
	}

}