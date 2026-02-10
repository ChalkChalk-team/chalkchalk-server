package com.writingboard.server.global.storage.dto;

public record PresignedUploadResponse(
        String uploadUrl,
        String storageKey
) {
}
