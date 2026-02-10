package com.writingboard.server.global.storage.dto;

import jakarta.validation.constraints.NotBlank;

public record PresignedUploadRequest(
        @NotBlank(message = "폴더를 지정해주세요.")
        String folder,

        @NotBlank(message = "파일 이름을 입력해주세요.")
        String fileName,

        @NotBlank(message = "Content-Type을 입력해주세요.")
        String contentType
) {
}
