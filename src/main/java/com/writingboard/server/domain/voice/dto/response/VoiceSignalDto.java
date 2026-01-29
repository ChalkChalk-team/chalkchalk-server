package com.writingboard.server.domain.voice.dto.response;

import com.writingboard.server.domain.voice.enums.SignalType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class VoiceSignalDto {

    private String roomUuid;
    private Long senderId;
    private String senderName;
    private SignalType signalType;
    private String data;
    private Instant timestamp;

    @Builder
    public VoiceSignalDto(String roomUuid, Long senderId, String senderName,
                          SignalType signalType, String data, Instant timestamp) {
        this.roomUuid = roomUuid;
        this.senderId = senderId;
        this.senderName = senderName;
        this.signalType = signalType;
        this.data = data;
        this.timestamp = timestamp;
    }

    public static VoiceSignalDto of(String roomUuid, Long senderId, String senderName,
                                    SignalType signalType, String data) {
        return VoiceSignalDto.builder()
                .roomUuid(roomUuid)
                .senderId(senderId)
                .senderName(senderName)
                .signalType(signalType)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }
}
