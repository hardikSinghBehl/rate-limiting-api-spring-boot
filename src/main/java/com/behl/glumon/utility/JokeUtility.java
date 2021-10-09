package com.behl.glumon.utility;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JokeUtility {

    public static String generate() {
        final var response = new JSONObject();
        try {
            response.put("joke", "My life.");
        } catch (JSONException e) {
            log.error("Unable to generate JSON response");
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
        }
        return response.toString();
    }

}
