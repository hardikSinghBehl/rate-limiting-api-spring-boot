package com.behl.glumon.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserAccountCreationRequestDto {

    private final String emailId;
    private final String password;
    private final UUID planId;

}
