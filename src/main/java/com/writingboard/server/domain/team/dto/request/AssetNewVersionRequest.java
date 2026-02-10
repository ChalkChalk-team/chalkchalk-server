package com.writingboard.server.domain.team.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssetNewVersionRequest {

    @NotBlank(message = "스토리지 키는 필수입니다")
    @Size(max = 500, message = "스토리지 키는 500자 이내여야 합니다")
    private String storageKey;
}
