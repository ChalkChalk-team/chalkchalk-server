package com.writingboard.server.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserIdAvailabilityResponse {

    private boolean available;
    private String reason;

    public static UserIdAvailabilityResponse available() {
        return new UserIdAvailabilityResponse(true, null);
    }

    public static UserIdAvailabilityResponse duplicate() {
        return new UserIdAvailabilityResponse(false, "DUPLICATE");
    }
}
