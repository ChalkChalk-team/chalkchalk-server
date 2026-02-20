package com.writingboard.server.domain.team.service;

import com.writingboard.server.domain.meeting.repository.RoomRepository;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.domain.team.entity.Team;
import com.writingboard.server.domain.team.entity.enums.TeamMemberStatus;
import com.writingboard.server.domain.team.entity.enums.TeamRole;
import com.writingboard.server.domain.team.exception.TeamException;
import com.writingboard.server.domain.team.repository.TeamAssetRepository;
import com.writingboard.server.domain.team.repository.TeamInvitationRepository;
import com.writingboard.server.domain.team.repository.TeamMemberRepository;
import com.writingboard.server.domain.team.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService 단위 테스트")
class TeamServiceTest {

    @InjectMocks
    private TeamService teamService;

    @Mock private TeamRepository teamRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private TeamAssetRepository teamAssetRepository;
    @Mock private TeamInvitationRepository teamInvitationRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private MemberRepository memberRepository;

    private static final Long OWNER_ID = 1L;
    private static final Long TEAM_ID = 10L;
    private static final Long NON_OWNER_ID = 2L;

    private Team team;

    @BeforeEach
    void setUp() {
        team = mock(Team.class);
        lenient().when(team.getId()).thenReturn(TEAM_ID);
    }

    @Nested
    @DisplayName("deleteTeam")
    class DeleteTeam {

        @Test
        @DisplayName("성공: OWNER가 팀 삭제 시 자식 엔티티 삭제 후 팀 삭제")
        void deleteTeam_성공_OWNER가_팀삭제() {
            // given
            given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));
            given(teamMemberRepository.existsByTeamIdAndMemberIdAndStatusAndRoleIn(
                    TEAM_ID, OWNER_ID, TeamMemberStatus.ACTIVE, List.of(TeamRole.OWNER)))
                    .willReturn(true);

            // when
            teamService.deleteTeam(OWNER_ID, TEAM_ID);

            // then - 삭제 순서 검증: Room → TeamAsset → TeamInvitation → TeamMember → Team
            InOrder inOrder = inOrder(roomRepository, teamAssetRepository,
                    teamInvitationRepository, teamMemberRepository, teamRepository);
            inOrder.verify(roomRepository).deleteAllByTeamId(TEAM_ID);
            inOrder.verify(teamAssetRepository).deleteAllByTeamId(TEAM_ID);
            inOrder.verify(teamInvitationRepository).deleteAllByTeamId(TEAM_ID);
            inOrder.verify(teamMemberRepository).deleteAllByTeamId(TEAM_ID);
            inOrder.verify(teamRepository).delete(team);
        }

        @Test
        @DisplayName("실패: OWNER가 아닌 멤버가 팀 삭제 시도 시 TeamException 발생")
        void deleteTeam_실패_OWNER가_아닌_경우() {
            // given
            given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));
            given(teamMemberRepository.existsByTeamIdAndMemberIdAndStatusAndRoleIn(
                    TEAM_ID, NON_OWNER_ID, TeamMemberStatus.ACTIVE, List.of(TeamRole.OWNER)))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() -> teamService.deleteTeam(NON_OWNER_ID, TEAM_ID))
                    .isInstanceOf(TeamException.class);

            // 삭제 로직이 실행되지 않음을 검증
            verify(roomRepository, never()).deleteAllByTeamId(anyLong());
            verify(teamMemberRepository, never()).deleteAllByTeamId(anyLong());
            verify(teamRepository, never()).delete(any(Team.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 팀 삭제 시도 시 TeamException 발생")
        void deleteTeam_실패_팀이_존재하지_않는_경우() {
            // given
            given(teamRepository.findById(TEAM_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamService.deleteTeam(OWNER_ID, TEAM_ID))
                    .isInstanceOf(TeamException.class);
        }
    }
}
