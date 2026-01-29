package com.writingboard.server.domain.team.service;

import com.writingboard.server.domain.team.dto.request.RoleChangeRequest;
import com.writingboard.server.domain.team.dto.response.TeamMemberResponse;
import com.writingboard.server.domain.team.entity.TeamMember;
import com.writingboard.server.domain.team.entity.enums.TeamMemberStatus;
import com.writingboard.server.domain.team.entity.enums.TeamRole;
import com.writingboard.server.domain.team.exception.TeamErrorCode;
import com.writingboard.server.domain.team.exception.TeamException;
import com.writingboard.server.domain.team.repository.TeamMemberRepository;
import com.writingboard.server.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamMemberService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public List<TeamMemberResponse> getTeamMembers(Long memberId, Long teamId) {
        validateTeamExists(teamId);
        validateTeamMember(teamId, memberId);

        List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatus(teamId, TeamMemberStatus.ACTIVE);
        return members.stream()
                .map(TeamMemberResponse::of)
                .toList();
    }

    @Transactional
    public void leaveTeam(Long memberId, Long teamId) {
        validateTeamExists(teamId);

        TeamMember teamMember = getTeamMember(teamId, memberId);

        if (teamMember.isOwner()) {
            throw new TeamException(TeamErrorCode.OWNER_CANNOT_LEAVE);
        }

        teamMember.leave();
    }

    @Transactional
    public void kickMember(Long memberId, Long teamId, Long targetMemberId) {
        validateTeamExists(teamId);
        validateAdminOrOwner(teamId, memberId);

        if (memberId.equals(targetMemberId)) {
            throw new TeamException(TeamErrorCode.CANNOT_KICK_SELF);
        }

        TeamMember targetTeamMember = getTeamMember(teamId, targetMemberId);

        if (targetTeamMember.isOwner()) {
            throw new TeamException(TeamErrorCode.CANNOT_KICK_OWNER);
        }

        targetTeamMember.leave();
    }

    @Transactional
    public TeamMemberResponse changeRole(Long memberId, Long teamId, Long targetMemberId, RoleChangeRequest request) {
        validateTeamExists(teamId);
        validateAdminOrOwner(teamId, memberId);

        TeamMember requester = getTeamMember(teamId, memberId);
        TeamMember targetMember = getTeamMember(teamId, targetMemberId);

        // OWNER 권한 변경 불가
        if (targetMember.isOwner()) {
            throw new TeamException(TeamErrorCode.CANNOT_CHANGE_OWNER_ROLE);
        }

        // OWNER로 변경 시도 불가
        if (request.getNewRole() == TeamRole.OWNER) {
            throw new TeamException(TeamErrorCode.CANNOT_CHANGE_OWNER_ROLE);
        }

        // 자신의 권한 강등 불가 (ADMIN이 자신을 MEMBER로 변경 시도)
        if (memberId.equals(targetMemberId) && request.getNewRole() == TeamRole.MEMBER && requester.isAdmin()) {
            throw new TeamException(TeamErrorCode.CANNOT_DEMOTE_SELF);
        }

        // 권한 변경
        if (request.getNewRole() == TeamRole.ADMIN) {
            targetMember.promoteToAdmin();
        } else if (request.getNewRole() == TeamRole.MEMBER) {
            targetMember.demoteToMember();
        }

        return TeamMemberResponse.of(targetMember);
    }

    // Helper methods
    private void validateTeamExists(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new TeamException(TeamErrorCode.TEAM_NOT_FOUND);
        }
    }

    private TeamMember getTeamMember(Long teamId, Long memberId) {
        return teamMemberRepository.findByTeamIdAndMemberIdAndStatus(teamId, memberId, TeamMemberStatus.ACTIVE)
                .orElseThrow(() -> new TeamException(TeamErrorCode.NOT_TEAM_MEMBER));
    }

    private void validateTeamMember(Long teamId, Long memberId) {
        boolean isMember = teamMemberRepository.existsByTeamIdAndMemberIdAndStatus(teamId, memberId, TeamMemberStatus.ACTIVE);
        if (!isMember) {
            throw new TeamException(TeamErrorCode.NOT_TEAM_MEMBER);
        }
    }

    private void validateAdminOrOwner(Long teamId, Long memberId) {
        boolean isAdminOrOwner = teamMemberRepository.existsByTeamIdAndMemberIdAndStatusAndRoleIn(
                teamId, memberId, TeamMemberStatus.ACTIVE, List.of(TeamRole.OWNER, TeamRole.ADMIN));
        if (!isAdminOrOwner) {
            throw new TeamException(TeamErrorCode.NOT_TEAM_ADMIN);
        }
    }
}
