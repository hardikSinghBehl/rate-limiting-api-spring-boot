package com.behl.glumon.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class PlanResponseDto {

    private final UUID id;
    private final String name;
    private final Integer limitPerHour;

}
