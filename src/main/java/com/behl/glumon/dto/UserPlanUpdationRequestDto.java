package com.behl.glumon.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserPlanUpdationRequestDto {

    private final UUID planId;

}
