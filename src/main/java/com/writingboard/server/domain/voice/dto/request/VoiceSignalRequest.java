package com.writingboard.server.domain.voice.dto.request;

import com.writingboard.server.domain.voice.enums.SignalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VoiceSignalRequest {

    @NotNull(message = "시그널 타입은 필수입니다")
    private SignalType signalType;

    @NotBlank(message = "시그널 데이터는 필수입니다")
    @Size(max = 10000, message = "시그널 데이터는 최대 10000자까지 가능합니다")
    private String data;
}
