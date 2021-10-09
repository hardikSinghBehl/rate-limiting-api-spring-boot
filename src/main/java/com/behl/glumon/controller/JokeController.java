package com.behl.glumon.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.behl.glumon.utility.JokeUtility;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class JokeController {

    @GetMapping(value = "/joke", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "Returns a funny joke")
    public ResponseEntity<?> jokeRetreivalHandler(
            @Parameter(hidden = true) @RequestHeader(name = "Authorization", required = true) final String header) {
        return ResponseEntity.ok(JokeUtility.generate());
    }

}
