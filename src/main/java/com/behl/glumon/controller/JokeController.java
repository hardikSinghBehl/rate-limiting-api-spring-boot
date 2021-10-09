package com.behl.glumon.controller;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.behl.glumon.utility.JokeUtility;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class JokeController {

    @GetMapping(value = "/joke", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "Returns a funny joke")
    public ResponseEntity<?> jokeRetreivalHandler(
            @Parameter(hidden = true) @RequestHeader(name = "Authorization", required = true) final String header) {
        final var response = new JSONObject();
        try {
            response.put("joke", JokeUtility.generate());
        } catch (JSONException e) {
            log.error("Unable to generate JSON response");
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
        }
        return ResponseEntity.ok(response.toString());
    }

}
