package com.writingboard.server.domain.team.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCreateRequest {

    @NotBlank(message = "일정 제목은 필수입니다")
    @Size(max = 200, message = "일정 제목은 최대 200자까지 가능합니다")
    private String title;

    @Size(max = 1000, message = "일정 설명은 최대 1000자까지 가능합니다")
    private String description;

    @NotNull(message = "시작 시간은 필수입니다")
    private OffsetDateTime startTime;

    @NotNull(message = "종료 시간은 필수입니다")
    private OffsetDateTime endTime;

    @Valid
    private RecurrenceRuleRequest recurrenceRule;

    private Integer reminderMinutesBefore;
}
