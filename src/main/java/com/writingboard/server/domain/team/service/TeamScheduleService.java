package com.writingboard.server.domain.team.service;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.domain.team.dto.request.RecurrenceRuleRequest;
import com.writingboard.server.domain.team.dto.request.ScheduleCreateRequest;
import com.writingboard.server.domain.team.dto.request.ScheduleUpdateRequest;
import com.writingboard.server.domain.team.dto.response.ScheduleResponse;
import com.writingboard.server.domain.team.entity.RecurrenceRule;
import com.writingboard.server.domain.team.entity.Team;
import com.writingboard.server.domain.team.entity.TeamSchedule;
import com.writingboard.server.domain.team.entity.enums.TeamMemberStatus;
import com.writingboard.server.domain.team.exception.TeamErrorCode;
import com.writingboard.server.domain.team.exception.TeamException;
import com.writingboard.server.domain.team.repository.TeamMemberRepository;
import com.writingboard.server.domain.team.repository.TeamRepository;
import com.writingboard.server.domain.team.repository.TeamScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamScheduleService {

    private final TeamScheduleRepository teamScheduleRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;

    public List<ScheduleResponse> getSchedules(Long memberId, Long teamId,
                                                OffsetDateTime from, OffsetDateTime to) {
        validateTeamMember(teamId, memberId);

        List<TeamSchedule> schedules = teamScheduleRepository.findByTeamIdAndDateRange(
                teamId, from, to, from);

        return schedules.stream()
                .map(ScheduleResponse::of)
                .toList();
    }

    @Transactional
    public ScheduleResponse createSchedule(Long memberId, Long teamId, ScheduleCreateRequest request) {
        Team team = getTeamById(teamId);
        validateTeamMember(teamId, memberId);
        Member creator = getMemberById(memberId);

        RecurrenceRule recurrenceRule = toRecurrenceRule(request.getRecurrenceRule());

        TeamSchedule schedule = TeamSchedule.create(
                team, creator,
                request.getTitle(), request.getDescription(),
                request.getStartTime(), request.getEndTime(),
                recurrenceRule, request.getReminderMinutesBefore()
        );

        teamScheduleRepository.save(schedule);
        return ScheduleResponse.of(schedule);
    }

    @Transactional
    public ScheduleResponse updateSchedule(Long memberId, Long teamId, Long scheduleId,
                                            ScheduleUpdateRequest request) {
        validateTeamMember(teamId, memberId);

        TeamSchedule schedule = teamScheduleRepository.findByIdAndTeamId(scheduleId, teamId)
                .orElseThrow(() -> new TeamException(TeamErrorCode.SCHEDULE_NOT_FOUND));

        RecurrenceRule recurrenceRule = toRecurrenceRule(request.getRecurrenceRule());

        schedule.update(
                request.getTitle(), request.getDescription(),
                request.getStartTime(), request.getEndTime(),
                recurrenceRule, request.getReminderMinutesBefore()
        );

        return ScheduleResponse.of(schedule);
    }

    @Transactional
    public void deleteSchedule(Long memberId, Long teamId, Long scheduleId) {
        validateTeamMember(teamId, memberId);

        TeamSchedule schedule = teamScheduleRepository.findByIdAndTeamId(scheduleId, teamId)
                .orElseThrow(() -> new TeamException(TeamErrorCode.SCHEDULE_NOT_FOUND));

        teamScheduleRepository.delete(schedule);
    }

    private RecurrenceRule toRecurrenceRule(RecurrenceRuleRequest request) {
        if (request == null) {
            return null;
        }
        return RecurrenceRule.create(
                request.getFrequency(),
                request.getInterval(),
                request.getDaysOfWeek(),
                request.getEndDate()
        );
    }

    private Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamException(TeamErrorCode.TEAM_NOT_FOUND));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new TeamException(TeamErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateTeamMember(Long teamId, Long memberId) {
        boolean isMember = teamMemberRepository.existsByTeamIdAndMemberIdAndStatus(
                teamId, memberId, TeamMemberStatus.ACTIVE);
        if (!isMember) {
            throw new TeamException(TeamErrorCode.NOT_TEAM_MEMBER);
        }
    }
}
