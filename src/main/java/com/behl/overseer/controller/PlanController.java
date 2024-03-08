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

import com.behl.overseer.configuration.BypassRateLimit;
import com.behl.overseer.configuration.PublicEndpoint;
import com.behl.overseer.dto.ExceptionResponseDto;
import com.behl.overseer.dto.PlanResponseDto;
import com.behl.overseer.dto.PlanUpdationRequestDto;
import com.behl.overseer.service.PlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Plan Management", description = "Endpoints for managing and retrieving available plan details")
public class PlanController {

	private final PlanService planService;

	@PublicEndpoint
	@GetMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Retrieves all available plans", description = "Retrieves the list of available plans in the system")
	@ApiResponse(responseCode = "200", description = "Plans retrieved successfully")
	public ResponseEntity<List<PlanResponseDto>> retrieve() {
		return ResponseEntity.ok(planService.retrieve());
	}

	@BypassRateLimit
	@PutMapping(value = "/plan")
	@Operation(summary = "Update user plan", description = "Updates an existing plan of an authenticated user")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Plan updated successfully",
					content = @Content(schema = @Schema(implementation = Void.class))),
			@ApiResponse(responseCode = "404", description = "No plan exists in the system with provided-id",
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
			@ApiResponse(responseCode = "429", description = "API rate limit exhausted",
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request body",
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))})
	public ResponseEntity<HttpStatus> update(@Valid @RequestBody final PlanUpdationRequestDto planUpdationRequest) {
		planService.update(planUpdationRequest);
		return ResponseEntity.ok().build();
	}

}