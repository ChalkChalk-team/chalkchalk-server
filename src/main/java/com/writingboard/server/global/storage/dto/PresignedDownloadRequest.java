package com.writingboard.server.global.storage.dto;

import jakarta.validation.constraints.NotBlank;

public record PresignedDownloadRequest(
        @NotBlank(message = "Storage key를 입력해주세요.")
        String storageKey
) {
}
