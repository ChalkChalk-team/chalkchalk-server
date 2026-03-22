package com.writingboard.server.domain.teamchat.dto.response;

import com.writingboard.server.domain.teamchat.document.TeamChatMessage;
import com.writingboard.server.domain.teamchat.enums.TeamChatMessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class TeamChatMessageResponse {

    private String messageId;
    private Long teamId;
    private Long senderId;
    private String senderName;
    private String content;
    private TeamChatMessageType type;
    private Instant createdAt;

    @Builder
    public TeamChatMessageResponse(String messageId, Long teamId, Long senderId,
                                   String senderName, String content,
                                   TeamChatMessageType type, Instant createdAt) {
        this.messageId = messageId;
        this.teamId = teamId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.type = type;
        this.createdAt = createdAt;
    }

    public static TeamChatMessageResponse from(TeamChatMessage message) {
        return TeamChatMessageResponse.builder()
                .messageId(message.getId())
                .teamId(message.getTeamId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .type(message.getType())
                .createdAt(message.getTimestamp())
                .build();
    }
}
