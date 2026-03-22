package com.writingboard.server.domain.team.entity;

import com.writingboard.server.domain.team.entity.enums.RecurrenceFrequency;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecurrenceRule {

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_frequency", length = 20)
    private RecurrenceFrequency frequency;

    @Column(name = "recurrence_interval")
    private Integer interval;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "schedule_days_of_week",
            joinColumns = @JoinColumn(name = "schedule_id"),
            foreignKey = @ForeignKey(name = "fk_schedule_days_schedule")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 10)
    private List<DayOfWeek> daysOfWeek = new ArrayList<>();

    @Column(name = "recurrence_end_date")
    private LocalDate endDate;

    public static RecurrenceRule create(RecurrenceFrequency frequency, Integer interval,
                                        List<DayOfWeek> daysOfWeek, LocalDate endDate) {
        RecurrenceRule rule = new RecurrenceRule();
        rule.frequency = frequency;
        rule.interval = interval != null ? interval : 1;
        rule.daysOfWeek = daysOfWeek != null ? new ArrayList<>(daysOfWeek) : new ArrayList<>();
        rule.endDate = endDate;
        return rule;
    }

    public void update(RecurrenceFrequency frequency, Integer interval,
                       List<DayOfWeek> daysOfWeek, LocalDate endDate) {
        this.frequency = frequency;
        this.interval = interval != null ? interval : 1;
        this.daysOfWeek = daysOfWeek != null ? new ArrayList<>(daysOfWeek) : new ArrayList<>();
        this.endDate = endDate;
    }
}
