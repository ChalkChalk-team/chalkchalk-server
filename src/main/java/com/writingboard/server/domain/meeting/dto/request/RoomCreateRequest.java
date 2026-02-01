package com.writingboard.server.domain.meeting.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoomCreateRequest {

    @NotNull
    private Long teamId;

    @NotBlank(message = "회의실 제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이내여야 합니다")
    private String title;

    @Size(min = 4, max = 20, message = "비밀번호는 4-20자여야 합니다")
    private String password;
}
