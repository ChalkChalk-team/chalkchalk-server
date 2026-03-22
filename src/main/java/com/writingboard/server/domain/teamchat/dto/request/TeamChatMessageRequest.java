package com.writingboard.server.domain.teamchat.dto.request;

import com.writingboard.server.domain.teamchat.enums.TeamChatMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamChatMessageRequest {

    @NotBlank(message = "메시지 내용은 필수입니다")
    @Size(max = 2000, message = "메시지는 최대 2000자까지 가능합니다")
    private String content;

    @NotNull(message = "메시지 타입은 필수입니다")
    private TeamChatMessageType type;
}
