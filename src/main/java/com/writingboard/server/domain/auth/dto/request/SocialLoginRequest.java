package com.writingboard.server.domain.auth.dto.request;

import com.writingboard.server.domain.member.entity.enums.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SocialLoginRequest {

    @NotNull(message = "provider는 필수입니다")
    private AuthProvider provider;

    @NotBlank(message = "idToken은 필수입니다")
    private String idToken;
}
