package com.writingboard.server.domain.team.service;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.domain.team.dto.request.DirectInviteRequest;
import com.writingboard.server.domain.team.dto.request.TeamInviteLinkRequest;
import com.writingboard.server.domain.team.dto.response.TeamInvitationResponse;
import com.writingboard.server.domain.team.dto.response.TeamInviteLinkResponse;
import com.writingboard.server.domain.team.entity.Team;
import com.writingboard.server.domain.team.entity.TeamInvitation;
import com.writingboard.server.domain.team.entity.TeamMember;
import com.writingboard.server.domain.team.entity.enums.TeamInvitationStatus;
import com.writingboard.server.domain.team.entity.enums.TeamMemberStatus;
import com.writingboard.server.domain.team.entity.enums.TeamRole;
import com.writingboard.server.domain.team.exception.TeamErrorCode;
import com.writingboard.server.domain.team.exception.TeamException;
import com.writingboard.server.domain.team.repository.TeamInvitationRepository;
import com.writingboard.server.domain.team.repository.TeamMemberRepository;
import com.writingboard.server.domain.team.repository.TeamRepository;
import com.writingboard.server.global.notification.ApnsPushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamInvitationService {

    private static final int DEFAULT_EXPIRE_MINUTES = 60;

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamInvitationRepository teamInvitationRepository;
    private final MemberRepository memberRepository;
    private final ApnsPushNotificationService pushNotificationService;

    @Transactional
    public TeamInviteLinkResponse createLinkInvitation(Long memberId, Long teamId, TeamInviteLinkRequest request) {
        Team team = getTeamById(teamId);
        Member inviter = getMemberById(memberId);
        validateAdminOrOwner(teamId, memberId);

        int expireMinutes = request != null && request.getExpiresInMinutes() != null
                ? request.getExpiresInMinutes() : DEFAULT_EXPIRE_MINUTES;

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(expireMinutes, ChronoUnit.MINUTES);

        TeamInvitation invitation = TeamInvitation.createLinkInvitation(team, inviter, token, expiresAt);
        teamInvitationRepository.save(invitation);

        return TeamInviteLinkResponse.of(invitation);
    }

    @Transactional
    public TeamInvitationResponse createDirectInvitation(Long memberId, Long teamId, DirectInviteRequest request) {
        Team team = getTeamById(teamId);
        Member inviter = getMemberById(memberId);

        validateAdminOrOwner(teamId, memberId);

        // 초대 대상 찾기
        Member invitee = null;
        if (request.getMemberId() != null) {
            invitee = getMemberById(request.getMemberId());
        } else if (request.getMemberUserId() != null && !request.getMemberUserId().isBlank()) {
            invitee = memberRepository.findByUserId(request.getMemberUserId())
                    .orElseThrow(() -> new TeamException(TeamErrorCode.MEMBER_NOT_FOUND));
        } else {
            throw new TeamException(TeamErrorCode.MEMBER_NOT_FOUND);
        }

        // 이미 팀 멤버인지 확인
        if (teamMemberRepository.existsByTeamIdAndMemberIdAndStatus(teamId, invitee.getId(), TeamMemberStatus.ACTIVE)) {
            throw new TeamException(TeamErrorCode.ALREADY_TEAM_MEMBER);
        }

        // 이미 초대된 상태인지 확인
        if (teamInvitationRepository.existsByTeamIdAndInviteeIdAndStatus(teamId, invitee.getId(), TeamInvitationStatus.PENDING)) {
            throw new TeamException(TeamErrorCode.ALREADY_INVITED);
        }

        TeamInvitation invitation = TeamInvitation.createDirectInvitation(team, inviter, invitee);
        teamInvitationRepository.save(invitation);

        // 푸시 알림 전송 (실패해도 초대 생성은 성공)
        sendInvitationPushNotification(invitee, inviter, team);

        return TeamInvitationResponse.of(invitation);
    }

    @Transactional
    public void acceptLinkInvitation(Long memberId, String inviteToken) {
        TeamInvitation invitation = teamInvitationRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new TeamException(TeamErrorCode.INVITATION_NOT_FOUND));

        invitation.checkAndExpire();

        if (!invitation.isUsable()) {
            throw new TeamException(TeamErrorCode.INVITATION_EXPIRED);
        }

        Member member = getMemberById(memberId);
        Long teamId = invitation.getTeam().getId();

        // 이미 팀 멤버인지 확인
        if (teamMemberRepository.existsByTeamIdAndMemberIdAndStatus(teamId, memberId, TeamMemberStatus.ACTIVE)) {
            throw new TeamException(TeamErrorCode.ALREADY_TEAM_MEMBER);
        }

        // 기존에 LEFT 상태인 멤버인지 확인
        TeamMember existingMember = teamMemberRepository.findByTeamIdAndMemberId(teamId, memberId).orElse(null);
        if (existingMember != null) {
            existingMember.activate();
        } else {
            TeamMember newMember = TeamMember.createMember(invitation.getTeam(), member);
            teamMemberRepository.save(newMember);
        }

        // 링크 초대는 여러 번 사용 가능하므로 상태 변경 안함
    }

    @Transactional
    public void acceptDirectInvitation(Long memberId, Long invitationId) {
        TeamInvitation invitation = teamInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new TeamException(TeamErrorCode.INVITATION_NOT_FOUND));

        if (!invitation.isDirectInvitation()) {
            throw new TeamException(TeamErrorCode.INVALID_INVITATION);
        }

        if (invitation.getInvitee() == null || !invitation.getInvitee().getId().equals(memberId)) {
            throw new TeamException(TeamErrorCode.INVALID_INVITATION);
        }

        if (!invitation.isUsable()) {
            throw new TeamException(TeamErrorCode.INVITATION_EXPIRED);
        }

        Long teamId = invitation.getTeam().getId();

        // 이미 팀 멤버인지 확인
        if (teamMemberRepository.existsByTeamIdAndMemberIdAndStatus(teamId, memberId, TeamMemberStatus.ACTIVE)) {
            throw new TeamException(TeamErrorCode.ALREADY_TEAM_MEMBER);
        }

        Member member = getMemberById(memberId);

        // 기존에 LEFT 상태인 멤버인지 확인
        TeamMember existingMember = teamMemberRepository.findByTeamIdAndMemberId(teamId, memberId).orElse(null);
        if (existingMember != null) {
            existingMember.activate();
        } else {
            TeamMember newMember = TeamMember.createMember(invitation.getTeam(), member);
            teamMemberRepository.save(newMember);
        }

        invitation.accept();
    }

    @Transactional
    public void rejectInvitation(Long memberId, Long invitationId) {
        TeamInvitation invitation = teamInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new TeamException(TeamErrorCode.INVITATION_NOT_FOUND));

        if (!invitation.isDirectInvitation()) {
            throw new TeamException(TeamErrorCode.INVALID_INVITATION);
        }

        if (invitation.getInvitee() == null || !invitation.getInvitee().getId().equals(memberId)) {
            throw new TeamException(TeamErrorCode.INVALID_INVITATION);
        }

        invitation.reject();
    }

    public List<TeamInvitationResponse> getMyPendingInvitations(Long memberId) {
        List<TeamInvitation> invitations = teamInvitationRepository.findByInviteeIdAndStatus(memberId, TeamInvitationStatus.PENDING);
        return invitations.stream()
                .map(TeamInvitationResponse::of)
                .toList();
    }

    public List<TeamInvitationResponse> getTeamInvitations(Long memberId, Long teamId) {
        validateTeamExists(teamId);
        validateAdminOrOwner(teamId, memberId);

        List<TeamInvitation> invitations = teamInvitationRepository.findByTeamIdAndStatus(teamId, TeamInvitationStatus.PENDING);
        return invitations.stream()
                .map(TeamInvitationResponse::of)
                .toList();
    }

    // Helper method for push notification
    private void sendInvitationPushNotification(Member invitee, Member inviter, Team team) {
        try {
            String inviterName = inviter.getDisplayName();
            String teamName = team.getName();

            String title = "팀 초대";
            String body = String.format("%s님이 %s 팀에 초대했습니다.",
                inviterName != null ? inviterName : "Unknown",
                teamName != null ? teamName : "Unknown"
            );

            log.info("Sending team invitation push notification - invitee: {}, team: {}",
                invitee.getId(), team.getId());

            pushNotificationService.sendToMember(invitee.getId(), title, body);

        } catch (Exception e) {
            // 로그만 남김
            log.warn("Failed to send team invitation push notification - invitee: {}, team: {}",
                invitee.getId(), team.getId(), e);
        }
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

    private void validateTeamExists(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new TeamException(TeamErrorCode.TEAM_NOT_FOUND);
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
