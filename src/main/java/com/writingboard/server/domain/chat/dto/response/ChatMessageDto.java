package com.writingboard.server.domain.chat.dto.response;

import com.writingboard.server.domain.chat.document.ChatMessage;
import com.writingboard.server.domain.chat.enums.MessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class ChatMessageDto {

    private String id;
    private String roomUuid;
    private Long senderId;
    private String senderUserId;
    private String senderName;
    private String senderProfileImage;
    private String content;
    private MessageType messageType;
    private Instant timestamp;

    @Builder
    public ChatMessageDto(String id, String roomUuid, Long senderId, String senderUserId,
                          String senderName, String senderProfileImage, String content,
                          MessageType messageType, Instant timestamp) {
        this.id = id;
        this.roomUuid = roomUuid;
        this.senderId = senderId;
        this.senderUserId = senderUserId;
        this.senderName = senderName;
        this.senderProfileImage = senderProfileImage;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = timestamp;
    }

    public static ChatMessageDto from(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .id(chatMessage.getId())
                .roomUuid(chatMessage.getRoomUuid())
                .senderId(chatMessage.getSenderId())
                .senderUserId(chatMessage.getSenderUserId())
                .senderName(chatMessage.getSenderName())
                .senderProfileImage(chatMessage.getSenderProfileImage())
                .content(chatMessage.getContent())
                .messageType(chatMessage.getMessageType())
                .timestamp(chatMessage.getTimestamp())
                .build();
    }
}
