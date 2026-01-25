package com.writingboard.server.domain.meeting.dto.response;

import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RoomSummaryResponse {

    private String roomUuid;
    private String title;
    private String status;
    private boolean hasPassword;
    private String hostName;
    private int participantCount;
    private Instant createdAt;

    public static RoomSummaryResponse of(Room room) {
        int activeCount = (int) room.getParticipants().stream()
                .filter(p -> p.getState() == ParticipantState.JOINED)
                .count();

        return RoomSummaryResponse.builder()
                .roomUuid(room.getRoomUuid())
                .title(room.getTitle())
                .status(room.getStatus().name())
                .hasPassword(room.getPasswordHash() != null)
                .hostName(room.getHost().getName())
                .participantCount(activeCount)
                .createdAt(room.getCreatedAt())
                .build();
    }
}
