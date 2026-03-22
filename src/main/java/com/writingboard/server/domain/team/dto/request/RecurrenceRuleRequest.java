package com.writingboard.server.domain.team.dto.request;

import com.writingboard.server.domain.team.entity.enums.RecurrenceFrequency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecurrenceRuleRequest {

    @NotNull(message = "반복 주기는 필수입니다")
    private RecurrenceFrequency frequency;

    @Min(value = 1, message = "반복 간격은 1 이상이어야 합니다")
    private Integer interval;

    private List<DayOfWeek> daysOfWeek;

    private LocalDate endDate;
}
