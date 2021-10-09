package com.behl.glumon.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.behl.glumon.dto.UserAccountCreationRequestDto;
import com.behl.glumon.dto.UserLoginRequestDto;
import com.behl.glumon.dto.UserLoginSuccessDto;
import com.behl.glumon.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    @PostMapping(value = "/sign-up")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "Creates a user account in the system")
    public ResponseEntity<?> userAccountCreationHandler(
            @RequestBody(required = true) final UserAccountCreationRequestDto userAccountCreationRequestDto) {
        return userService.signUp(userAccountCreationRequestDto);
    }

    @PostMapping(value = "/login")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "Returns JWT corresponding to logged in user")
    public ResponseEntity<UserLoginSuccessDto> userLoginHandler(
            @RequestBody(required = true) final UserLoginRequestDto userLoginRequestDto) {
        return userService.login(userLoginRequestDto);
    }

}
