package com.writingboard.server.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterDeviceTokenRequest {

    @NotBlank(message = "apnsToken은 필수입니다.")
    private String apnsToken;
}
