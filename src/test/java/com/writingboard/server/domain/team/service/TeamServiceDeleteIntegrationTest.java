package com.writingboard.server.domain.team.service;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.domain.team.entity.Team;
import com.writingboard.server.domain.team.entity.TeamMember;
import com.writingboard.server.domain.team.entity.enums.TeamMemberStatus;
import com.writingboard.server.domain.team.repository.TeamAssetRepository;
import com.writingboard.server.domain.team.repository.TeamMemberRepository;
import com.writingboard.server.domain.team.repository.TeamRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.TransientObjectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("팀 삭제 통합 테스트 - TransientObjectException 재현 및 수정 검증")
class TeamServiceDeleteIntegrationTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamAssetRepository teamAssetRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    private Member owner;
    private Team team;
    private TeamMember ownerMember;

    @BeforeEach
    void setUp() {
        owner = Member.createGuest("device-001", "테스트유저");
        memberRepository.save(owner);

        team = Team.create(owner, "테스트팀", "테스트 설명");
        teamRepository.save(team);

        ownerMember = TeamMember.createOwner(team, owner);
        teamMemberRepository.save(ownerMember);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("버그 재현: TeamMember status 변경 후 Team 삭제 시 TransientObjectException 발생")
    void deleteTeam_기존로직_TransientObjectException_발생() {
        // given
        Team foundTeam = teamRepository.findById(team.getId()).orElseThrow();
        List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatus(
                foundTeam.getId(), TeamMemberStatus.ACTIVE);

        // when - 기존 로직: status 변경 후 Team 삭제
        members.forEach(TeamMember::leave);
        teamRepository.delete(foundTeam);

        // then - flush 시 TransientObjectException 발생
        assertThatThrownBy(() -> entityManager.flush())
                .rootCause()
                .isInstanceOf(TransientObjectException.class);
    }

    @Test
    @DisplayName("수정 검증: 자식 엔티티 hard delete 후 Team 삭제 시 정상 동작")
    void deleteTeam_수정로직_정상삭제() {
        // given
        Team foundTeam = teamRepository.findById(team.getId()).orElseThrow();

        // when - 수정된 로직: 자식 엔티티 hard delete 후 Team 삭제
        teamMemberRepository.deleteAllByTeamId(foundTeam.getId());
        teamRepository.delete(foundTeam);
        entityManager.flush();

        // then
        assertThat(teamRepository.findById(team.getId())).isEmpty();
        assertThat(teamMemberRepository.findByTeamIdAndStatus(team.getId(), TeamMemberStatus.ACTIVE)).isEmpty();
    }
}
