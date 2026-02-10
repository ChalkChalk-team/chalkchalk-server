package com.writingboard.server.domain.team.service;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.domain.team.dto.request.TeamCreateRequest;
import com.writingboard.server.domain.team.dto.request.TeamUpdateRequest;
import com.writingboard.server.domain.team.dto.response.TeamResponse;
import com.writingboard.server.domain.team.dto.response.TeamSummaryResponse;
import com.writingboard.server.domain.team.entity.Team;
import com.writingboard.server.domain.team.entity.TeamAsset;
import com.writingboard.server.domain.team.entity.TeamMember;
import com.writingboard.server.domain.team.entity.enums.AssetStatus;
import com.writingboard.server.domain.team.entity.enums.TeamMemberStatus;
import com.writingboard.server.domain.team.entity.enums.TeamRole;
import com.writingboard.server.domain.team.exception.TeamErrorCode;
import com.writingboard.server.domain.team.exception.TeamException;
import com.writingboard.server.domain.team.repository.TeamAssetRepository;
import com.writingboard.server.domain.team.repository.TeamMemberRepository;
import com.writingboard.server.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamAssetRepository teamAssetRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public TeamResponse createTeam(Long memberId, TeamCreateRequest request) {
        Member creator = getMemberById(memberId);

        Team team = Team.create(creator, request.getName(), request.getDescription());
        teamRepository.save(team);

        List<TeamMember> teamMembers = new ArrayList<>();

        // 생성자를 OWNER로 추가
        TeamMember ownerMember = TeamMember.createOwner(team, creator);
        teamMemberRepository.save(ownerMember);
        teamMembers.add(ownerMember);

        // memberIds로 초대된 멤버들 추가
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            for (Long invitedMemberId : request.getMemberIds()) {
                if (!invitedMemberId.equals(memberId)) {
                    Member invitedMember = getMemberById(invitedMemberId);
                    TeamMember tm = TeamMember.createMember(team, invitedMember);
                    teamMemberRepository.save(tm);
                    teamMembers.add(tm);
                }
            }
        }

        // memberNames로 초대된 멤버들 추가
        if (request.getMemberNames() != null && !request.getMemberNames().isEmpty()) {
            for (String memberName : request.getMemberNames()) {
                memberRepository.findByName(memberName).ifPresent(invitedMember -> {
                    if (!invitedMember.getId().equals(memberId) &&
                            teamMembers.stream().noneMatch(tm -> tm.getMember().getId().equals(invitedMember.getId()))) {
                        TeamMember tm = TeamMember.createMember(team, invitedMember);
                        teamMemberRepository.save(tm);
                        teamMembers.add(tm);
                    }
                });
            }
        }

        return TeamResponse.of(team, teamMembers);
    }

    public TeamResponse getTeam(Long memberId, Long teamId) {
        Team team = getTeamById(teamId);
        validateTeamMember(teamId, memberId);

        List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatus(teamId, TeamMemberStatus.ACTIVE);
        return TeamResponse.of(team, members);
    }

    public List<TeamSummaryResponse> getMyTeams(Long memberId) {
        List<TeamMember> myMemberships = teamMemberRepository.findByMemberIdAndStatus(memberId, TeamMemberStatus.ACTIVE);

        return myMemberships.stream()
                .map(membership -> {
                    Team team = membership.getTeam();
                    int memberCount = teamMemberRepository.findByTeamIdAndStatus(team.getId(), TeamMemberStatus.ACTIVE).size();
                    return TeamSummaryResponse.of(team, membership, memberCount);
                })
                .toList();
    }

    @Transactional
    public TeamResponse updateTeam(Long memberId, Long teamId, TeamUpdateRequest request) {
        Team team = getTeamById(teamId);
        validateAdminOrOwner(teamId, memberId);

        team.updateInfo(request.getName(), request.getDescription(), request.getTeamProfileImgUrl());

        List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatus(teamId, TeamMemberStatus.ACTIVE);
        return TeamResponse.of(team, members);
    }

    @Transactional
    public void deleteTeam(Long memberId, Long teamId) {
        Team team = getTeamById(teamId);
        validateOwner(teamId, memberId);

        // 모든 팀 자산 soft delete
        List<TeamAsset> assets = teamAssetRepository.findByTeamIdAndStatus(teamId, AssetStatus.ACTIVE);
        assets.forEach(TeamAsset::delete);

        // 모든 팀 멤버 상태를 LEFT로 변경
        List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatus(teamId, TeamMemberStatus.ACTIVE);
        members.forEach(TeamMember::leave);

        teamRepository.delete(team);
    }

    // Helper methods
    private Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamException(TeamErrorCode.TEAM_NOT_FOUND));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new TeamException(TeamErrorCode.MEMBER_NOT_FOUND));
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

    private void validateOwner(Long teamId, Long memberId) {
        boolean isOwner = teamMemberRepository.existsByTeamIdAndMemberIdAndStatusAndRoleIn(
                teamId, memberId, TeamMemberStatus.ACTIVE, List.of(TeamRole.OWNER));
        if (!isOwner) {
            throw new TeamException(TeamErrorCode.NOT_TEAM_OWNER);
        }
    }
}
