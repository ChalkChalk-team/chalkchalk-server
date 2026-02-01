package com.writingboard.server.domain.personal.dto.request;

import com.writingboard.server.domain.team.entity.enums.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PersonalAssetCreateRequest {

    @NotBlank(message = "자료 이름은 필수입니다")
    private String name;

    @NotNull(message = "자료 타입은 필수입니다")
    private AssetType type;

    private String storageKey;

    private Long sizeBytes;

    private Integer totalPages;
}
