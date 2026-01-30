package com.writingboard.server.domain.team.dto.request;

import com.writingboard.server.domain.team.entity.enums.TeamRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleChangeRequest {

    @NotNull(message = "변경할 역할은 필수입니다")
    private TeamRole newRole;
}
