package com.writingboard.server.domain.team.dto.response;

import com.writingboard.server.domain.team.entity.TeamSchedule;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.OffsetDateTime;

@Data
@Builder
public class ScheduleResponse {

    private Long scheduleId;
    private Long teamId;
    private String title;
    private String description;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private RecurrenceRuleResponse recurrenceRule;
    private Integer reminderMinutesBefore;
    private CreatedByInfo createdBy;
    private Instant createdAt;

    @Data
    @Builder
    public static class CreatedByInfo {
        private Long memberId;
        private String name;
    }

    public static ScheduleResponse of(TeamSchedule schedule) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getId())
                .teamId(schedule.getTeam().getId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .recurrenceRule(RecurrenceRuleResponse.of(schedule.getRecurrenceRule()))
                .reminderMinutesBefore(schedule.getReminderMinutesBefore())
                .createdBy(CreatedByInfo.builder()
                        .memberId(schedule.getCreatedBy().getId())
                        .name(schedule.getCreatedBy().getDisplayName())
                        .build())
                .createdAt(schedule.getCreatedAt())
                .build();
    }
}
