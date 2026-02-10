package com.writingboard.server.domain.meeting.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssetSaveRequest {

    @NotBlank(message = "스토리지 키는 필수입니다")
    private String storageKey;
}
