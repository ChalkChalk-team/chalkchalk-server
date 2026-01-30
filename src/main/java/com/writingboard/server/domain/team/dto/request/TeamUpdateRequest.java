package com.writingboard.server.domain.team.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeamUpdateRequest {

    @Size(max = 100, message = "팀 이름은 100자 이내여야 합니다")
    private String name;

    @Size(max = 500, message = "팀 설명은 500자 이내여야 합니다")
    private String description;

    private String teamProfileImgUrl;
}
