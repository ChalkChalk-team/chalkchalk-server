package com.writingboard.server.domain.auth.dto.request;

import com.writingboard.server.domain.member.entity.enums.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SocialLoginRequest {

    @NotNull(message = "provider는 필수입니다")
    private AuthProvider provider;

    @NotBlank(message = "idToken은 필수입니다")
    private String idToken;

    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "사용자 ID는 영문, 숫자, 밑줄만 사용 가능하며 3~20자여야 합니다.")
    private String userId;

    @Size(max = 30, message = "닉네임은 30자 이내여야 합니다.")
    private String nickname;
}
