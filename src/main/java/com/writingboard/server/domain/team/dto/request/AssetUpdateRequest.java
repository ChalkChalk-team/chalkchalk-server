package com.writingboard.server.domain.team.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssetUpdateRequest {

    @Size(max = 255, message = "자산 이름은 255자 이내여야 합니다")
    private String name;
}
