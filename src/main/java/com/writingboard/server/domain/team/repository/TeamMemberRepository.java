package com.writingboard.server.domain.team.repository;

import com.writingboard.server.domain.team.entity.TeamMember;
import com.writingboard.server.domain.team.entity.enums.TeamMemberStatus;
import com.writingboard.server.domain.team.entity.enums.TeamRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByTeamIdAndMemberId(Long teamId, Long memberId);

    Optional<TeamMember> findByTeamIdAndMemberIdAndStatus(Long teamId, Long memberId, TeamMemberStatus status);

    List<TeamMember> findByTeamIdAndStatus(Long teamId, TeamMemberStatus status);

    List<TeamMember> findByMemberIdAndStatus(Long memberId, TeamMemberStatus status);

    boolean existsByTeamIdAndMemberIdAndStatus(Long teamId, Long memberId, TeamMemberStatus status);

    boolean existsByTeamIdAndMemberIdAndRoleIn(Long teamId, Long memberId, List<TeamRole> roles);

    boolean existsByTeamIdAndMemberIdAndStatusAndRoleIn(Long teamId, Long memberId, TeamMemberStatus status, List<TeamRole> roles);
}
