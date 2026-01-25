package com.writingboard.server.domain.meeting.dto.response;

import com.writingboard.server.domain.meeting.entity.Room;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RoomCreateResponse {

    private String roomUuid;
    private String title;
    private boolean hasPassword;
    private Instant createdAt;

    public static RoomCreateResponse of(Room room) {
        return RoomCreateResponse.builder()
                .roomUuid(room.getRoomUuid())
                .title(room.getTitle())
                .hasPassword(room.getPasswordHash() != null)
                .createdAt(room.getCreatedAt())
                .build();
    }
}
