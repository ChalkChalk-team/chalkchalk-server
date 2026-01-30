package com.writingboard.server.domain.team.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TeamCreateRequest {

    @NotBlank(message = "팀 이름은 필수입니다")
    @Size(max = 100, message = "팀 이름은 100자 이내여야 합니다")
    private String name;

    @Size(max = 500, message = "팀 설명은 500자 이내여야 합니다")
    private String description;

    private List<Long> memberIds;

    private List<String> memberNames;
}
