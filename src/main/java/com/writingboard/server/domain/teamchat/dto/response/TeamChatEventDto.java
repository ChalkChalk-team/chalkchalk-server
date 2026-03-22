package com.writingboard.server.domain.teamchat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.writingboard.server.domain.teamchat.enums.TeamChatEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamChatEventDto {

    private TeamChatEventType eventType;
    private TeamChatMessageResponse message;
    private String messageId;
}
