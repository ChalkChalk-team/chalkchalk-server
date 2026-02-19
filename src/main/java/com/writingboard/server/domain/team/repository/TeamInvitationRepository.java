package com.writingboard.server.domain.team.repository;

import com.writingboard.server.domain.team.entity.TeamInvitation;
import com.writingboard.server.domain.team.entity.enums.TeamInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {

    Optional<TeamInvitation> findByInviteToken(String inviteToken);

    Optional<TeamInvitation> findByTeamIdAndInviteeIdAndStatus(Long teamId, Long inviteeId, TeamInvitationStatus status);

    List<TeamInvitation> findByTeamIdAndStatus(Long teamId, TeamInvitationStatus status);

    List<TeamInvitation> findByInviteeIdAndStatus(Long inviteeId, TeamInvitationStatus status);

    boolean existsByTeamIdAndInviteeIdAndStatus(Long teamId, Long inviteeId, TeamInvitationStatus status);

    boolean existsByInviteToken(String inviteToken);

    void deleteAllByTeamId(Long teamId);
}
