package com.writingboard.server.global.storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresignedUploadRequest(
        @NotNull(message = "팀 ID는 필수입니다.")
        Long teamId,

        @NotBlank(message = "파일 이름을 입력해주세요.")
        String fileName,

        @NotBlank(message = "Content-Type을 입력해주세요.")
        String contentType
) {
}
