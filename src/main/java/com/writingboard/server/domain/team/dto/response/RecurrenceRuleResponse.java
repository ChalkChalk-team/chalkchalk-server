package com.writingboard.server.domain.team.dto.response;

import com.writingboard.server.domain.team.entity.RecurrenceRule;
import com.writingboard.server.domain.team.entity.enums.RecurrenceFrequency;
import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class RecurrenceRuleResponse {

    private RecurrenceFrequency frequency;
    private Integer interval;
    private List<DayOfWeek> daysOfWeek;
    private LocalDate endDate;

    public static RecurrenceRuleResponse of(RecurrenceRule rule) {
        if (rule == null || rule.getFrequency() == null) {
            return null;
        }
        return RecurrenceRuleResponse.builder()
                .frequency(rule.getFrequency())
                .interval(rule.getInterval())
                .daysOfWeek(rule.getDaysOfWeek())
                .endDate(rule.getEndDate())
                .build();
    }
}
