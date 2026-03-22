package com.writingboard.server.domain.team.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamProfileUpdateRequest {

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(max = 30, message = "닉네임은 최대 30자까지 가능합니다")
    private String nickname;
}
