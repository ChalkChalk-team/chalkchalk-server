package com.writingboard.server.domain.meeting.dto.response;

import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.RoomParticipant;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RoomJoinResponse {

    private String roomUuid;
    private String title;
    private String myRole;
    private Instant joinedAt;

    public static RoomJoinResponse of(Room room, RoomParticipant participant) {
        return RoomJoinResponse.builder()
                .roomUuid(room.getRoomUuid())
                .title(room.getTitle())
                .myRole(participant.getRole().name())
                .joinedAt(participant.getJoinedAt())
                .build();
    }
}
