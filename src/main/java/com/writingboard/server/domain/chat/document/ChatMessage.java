package com.writingboard.server.domain.chat.document;

import com.writingboard.server.domain.chat.enums.MessageType;
import com.writingboard.server.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "chat_messages")
@CompoundIndex(name = "idx_room_timestamp", def = "{'roomUuid': 1, 'timestamp': -1}")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    private String id;

    @Indexed
    private String roomUuid;

    private Long senderId;
    private String senderName;
    private String senderProfileImage;

    private String content;
    private MessageType messageType;

    @Indexed
    private Instant timestamp;

    private ChatMessage(String roomUuid, Long senderId, String senderName,
                        String senderProfileImage, String content, MessageType messageType) {
        this.roomUuid = roomUuid;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderProfileImage = senderProfileImage;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = Instant.now();
    }

    public static ChatMessage createText(String roomUuid, Member sender, String content) {
        return new ChatMessage(
                roomUuid,
                sender.getId(),
                sender.getDisplayName(),
                sender.getProfileImageUrl(),
                content,
                MessageType.TEXT
        );
    }

    public static ChatMessage createSystem(String roomUuid, String content) {
        return new ChatMessage(
                roomUuid,
                null,
                "SYSTEM",
                null,
                content,
                MessageType.SYSTEM
        );
    }
}
