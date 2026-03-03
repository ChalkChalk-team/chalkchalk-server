package com.writingboard.server.domain.meeting.dto.response;

import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.RoomParticipant;
import com.writingboard.server.domain.member.entity.Member;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class RoomDetailResponse {

    private String roomUuid;
    private String title;
    private boolean hasPassword;
    private String status;
    private HostInfo host;
    private List<ParticipantInfo> participants;
    private Instant createdAt;
    private Instant lastActivityAt;

    public static RoomDetailResponse of(Room room, List<RoomParticipant> activeParticipants) {
        return RoomDetailResponse.builder()
                .roomUuid(room.getRoomUuid())
                .title(room.getTitle())
                .hasPassword(room.getPasswordHash() != null)
                .status(room.getStatus().name())
                .host(HostInfo.of(room.getHost()))
                .participants(activeParticipants.stream()
                        .map(ParticipantInfo::of)
                        .toList())
                .createdAt(room.getCreatedAt())
                .lastActivityAt(room.getLastActivityAt())
                .build();
    }

    @Data
    @Builder
    public static class HostInfo {
        private Long id;
        private String userId;
        private String name;
        private String email;

        public static HostInfo of(Member member) {
            return HostInfo.builder()
                    .id(member.getId())
                    .userId(member.getUserId())
                    .name(member.getDisplayName())
                    .email(member.getEmail())
                    .build();
        }
    }

    @Data
    @Builder
    public static class ParticipantInfo {
        private Long memberId;
        private String userId;
        private String name;
        private String role;
        private Instant joinedAt;

        public static ParticipantInfo of(RoomParticipant participant) {
            return ParticipantInfo.builder()
                    .memberId(participant.getMember().getId())
                    .userId(participant.getMember().getUserId())
                    .name(participant.getMember().getDisplayName())
                    .role(participant.getRole().name())
                    .joinedAt(participant.getJoinedAt())
                    .build();
        }
    }
}
