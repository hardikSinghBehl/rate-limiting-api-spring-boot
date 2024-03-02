package com.behl.overseer.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.behl.overseer.dto.JokeResponseDto;
import com.behl.overseer.utility.JokeGenerator;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class JokeController {

	private final JokeGenerator jokeGenerator;

	@GetMapping(value = "/joke", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JokeResponseDto> generate() {
		final var response = jokeGenerator.generate();
		return ResponseEntity.ok(response);
	}

}