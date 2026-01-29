package com.writingboard.server.domain.team.dto.request;

import lombok.Data;

@Data
public class DirectInviteRequest {

    private Long memberId;

    private String memberName;
}
