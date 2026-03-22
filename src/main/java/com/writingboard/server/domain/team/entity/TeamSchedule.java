package com.writingboard.server.domain.team.entity;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "team_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_schedule_team"))
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, foreignKey = @ForeignKey(name = "fk_schedule_member"))
    private Member createdBy;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private OffsetDateTime endTime;

    @Embedded
    private RecurrenceRule recurrenceRule;

    @Column(name = "reminder_minutes_before")
    private Integer reminderMinutesBefore;

    public static TeamSchedule create(Team team, Member createdBy, String title, String description,
                                       OffsetDateTime startTime, OffsetDateTime endTime,
                                       RecurrenceRule recurrenceRule, Integer reminderMinutesBefore) {
        TeamSchedule schedule = new TeamSchedule();
        schedule.team = team;
        schedule.createdBy = createdBy;
        schedule.title = title;
        schedule.description = description;
        schedule.startTime = startTime;
        schedule.endTime = endTime;
        schedule.recurrenceRule = recurrenceRule;
        schedule.reminderMinutesBefore = reminderMinutesBefore;
        return schedule;
    }

    public void update(String title, String description,
                       OffsetDateTime startTime, OffsetDateTime endTime,
                       RecurrenceRule recurrenceRule, Integer reminderMinutesBefore) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.recurrenceRule = recurrenceRule;
        this.reminderMinutesBefore = reminderMinutesBefore;
    }
}
