package com.behl.overseer.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.behl.overseer.dto.ExceptionResponseDto;
import com.behl.overseer.dto.JokeResponseDto;
import com.behl.overseer.utility.JokeGenerator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class JokeController {

	private final JokeGenerator jokeGenerator;

	@GetMapping(value = "/joke", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Generates a random unfunny joke")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Successfully generated random unfunny joke"),
			@ApiResponse(responseCode = "429", description = "API rate limit exhausted",
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))) })
	public ResponseEntity<JokeResponseDto> generate() {
		final var response = jokeGenerator.generate();
		return ResponseEntity.ok(response);
	}

}