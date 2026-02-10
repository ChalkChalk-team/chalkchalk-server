package com.writingboard.server.domain.meeting.dto.request;

import com.writingboard.server.domain.meeting.entity.enums.SavePolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoadPersonalAssetRequest {

    @NotNull(message = "개인 자료 ID는 필수입니다")
    private Long personalAssetId;

    @NotBlank(message = "스토리지 키는 필수입니다")
    @Size(max = 500, message = "스토리지 키는 500자 이내여야 합니다")
    private String storageKey;

    private SavePolicy savePolicy;
}
