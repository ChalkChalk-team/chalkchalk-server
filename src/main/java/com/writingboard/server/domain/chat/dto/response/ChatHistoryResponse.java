package com.writingboard.server.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ChatHistoryResponse {

    private List<ChatMessageDto> messages;
    private boolean hasMore;
    private Instant nextCursor;

    public static ChatHistoryResponse of(List<ChatMessageDto> messages, int requestedLimit) {
        boolean hasMore = messages.size() >= requestedLimit;
        Instant nextCursor = messages.isEmpty() ? null : messages.get(messages.size() - 1).getTimestamp();

        return ChatHistoryResponse.builder()
                .messages(messages)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .build();
    }
}
