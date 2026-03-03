package com.writingboard.server.domain.team.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DirectInviteRequest {

    private Long memberId;

    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "사용자 ID는 영문, 숫자, 밑줄만 사용 가능하며 3~20자여야 합니다.")
    private String memberUserId;
}
