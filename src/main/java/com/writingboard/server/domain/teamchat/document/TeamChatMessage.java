package com.writingboard.server.domain.teamchat.document;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.teamchat.enums.TeamChatMessageType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "team_chat_messages")
@CompoundIndex(name = "idx_team_timestamp", def = "{'teamId': 1, 'timestamp': -1}")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamChatMessage {

    @Id
    private String id;

    private Long teamId;
    private Long senderId;
    private String senderName;
    private String content;
    private TeamChatMessageType type;
    private Instant timestamp;

    private TeamChatMessage(Long teamId, Long senderId, String senderName,
                            String content, TeamChatMessageType type) {
        this.teamId = teamId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.type = type;
        this.timestamp = Instant.now();
    }

    public static TeamChatMessage createText(Long teamId, Member sender, String content) {
        return new TeamChatMessage(
                teamId,
                sender.getId(),
                sender.getDisplayName(),
                content,
                TeamChatMessageType.TEXT
        );
    }
}
