package com.writingboard.server.domain.team.dto.request;

import com.writingboard.server.domain.team.entity.enums.AssetSourceType;
import com.writingboard.server.domain.team.entity.enums.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssetCreateRequest {

    @NotNull(message = "파일 타입은 필수입니다")
    private AssetType type;

    @NotBlank(message = "파일 이름은 필수입니다")
    @Size(max = 255, message = "파일 이름은 255자 이내여야 합니다")
    private String name;

    @NotBlank(message = "스토리지 키는 필수입니다")
    @Size(max = 500, message = "스토리지 키는 500자 이내여야 합니다")
    private String storageKey;

    private Integer totalPages;

    private AssetSourceType sourceType = AssetSourceType.UPLOADED;

    private Long originPersonalAssetId;
}
