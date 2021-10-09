package com.behl.glumon.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserLoginRequestDto {

    private final String emailId;
    private final String password;

}
