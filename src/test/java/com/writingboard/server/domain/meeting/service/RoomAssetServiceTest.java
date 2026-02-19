package com.writingboard.server.domain.meeting.service;

import com.writingboard.server.domain.meeting.dto.request.AssetSaveRequest;
import com.writingboard.server.domain.meeting.dto.response.RoomAssetResponse;
import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.RoomAsset;
import com.writingboard.server.domain.meeting.entity.RoomParticipant;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantRole;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.meeting.entity.enums.SavePolicy;
import com.writingboard.server.domain.meeting.exception.MeetingException;
import com.writingboard.server.domain.meeting.repository.RoomAssetRepository;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.domain.personal.service.PersonalAssetService;
import com.writingboard.server.domain.team.entity.Team;
import com.writingboard.server.domain.team.entity.TeamAsset;
import com.writingboard.server.domain.team.entity.enums.AssetSourceType;
import com.writingboard.server.domain.team.entity.enums.AssetStatus;
import com.writingboard.server.domain.team.entity.enums.AssetType;
import com.writingboard.server.domain.team.repository.TeamAssetRepository;
import com.writingboard.server.global.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomAssetServiceTest {

    @InjectMocks
    private RoomAssetService roomAssetService;

    @Mock private RoomAssetRepository roomAssetRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomParticipantRepository participantRepository;
    @Mock private TeamAssetRepository teamAssetRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private PersonalAssetService personalAssetService;
    @Mock private StorageService storageService;

    private Member member;
    private Team team;
    private Room room;
    private RoomParticipant participant;
    private TeamAsset teamAsset;

    private static final Long MEMBER_ID = 1L;
    private static final String ROOM_UUID = "test-room-uuid";
    private static final Long ROOM_ASSET_ID = 10L;
    private static final String OLD_STORAGE_KEY = "teams/1/old-file.pdf";
    private static final String NEW_STORAGE_KEY = "teams/1/new-file.pdf";

    @BeforeEach
    void setUp() {
        member = mock(Member.class);
        lenient().when(member.getId()).thenReturn(MEMBER_ID);
        lenient().when(member.getName()).thenReturn("테스터");

        team = mock(Team.class);
        lenient().when(team.getId()).thenReturn(1L);

        room = mock(Room.class);
        lenient().when(room.getId()).thenReturn(1L);
        lenient().when(room.getTeam()).thenReturn(team);

        participant = mock(RoomParticipant.class);
        lenient().when(participant.getMember()).thenReturn(member);
        lenient().when(participant.getState()).thenReturn(ParticipantState.JOINED);
        lenient().when(participant.getRole()).thenReturn(ParticipantRole.PARTICIPANT);

        teamAsset = TeamAsset.create(team, member, AssetType.PDF, "test.pdf",
                AssetSourceType.UPLOADED, OLD_STORAGE_KEY, 5);
    }

    private void stubRoomAndParticipant() {
        given(roomRepository.findByRoomUuid(ROOM_UUID)).willReturn(Optional.of(room));
        given(participantRepository.findByRoomIdAndMemberId(room.getId(), MEMBER_ID))
                .willReturn(Optional.of(participant));
    }

    @Nested
    @DisplayName("saveAsset - OVERWRITE")
    class SaveAssetOverwrite {

        @Test
        @DisplayName("성공: 원본 TeamAsset의 storageKey가 새 키로 교체되고 기존 R2 파일이 삭제된다")
        void overwrite_success() {
            // given
            stubRoomAndParticipant();

            RoomAsset roomAsset = RoomAsset.createFromTeamAsset(room, teamAsset, member, SavePolicy.OVERWRITE);
            given(roomAssetRepository.findByIdAndRoomUuid(ROOM_ASSET_ID, ROOM_UUID))
                    .willReturn(Optional.of(roomAsset));

            AssetSaveRequest request = new AssetSaveRequest(NEW_STORAGE_KEY);

            // when
            RoomAssetResponse response = roomAssetService.saveAsset(MEMBER_ID, ROOM_UUID, ROOM_ASSET_ID, request);

            // then
            assertThat(teamAsset.getStorageKey()).isEqualTo(NEW_STORAGE_KEY);
            verify(storageService).deleteObject(OLD_STORAGE_KEY);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("성공: 기존 storageKey가 null이면 R2 삭제를 건너뛴다")
        void overwrite_nullOldKey_skipsDelete() {
            // given
            stubRoomAndParticipant();

            TeamAsset assetWithNullKey = TeamAsset.create(team, member, AssetType.PDF, "test.pdf",
                    AssetSourceType.UPLOADED, null, 5);
            RoomAsset roomAsset = RoomAsset.createFromTeamAsset(room, assetWithNullKey, member, SavePolicy.OVERWRITE);
            given(roomAssetRepository.findByIdAndRoomUuid(ROOM_ASSET_ID, ROOM_UUID))
                    .willReturn(Optional.of(roomAsset));

            AssetSaveRequest request = new AssetSaveRequest(NEW_STORAGE_KEY);

            // when
            roomAssetService.saveAsset(MEMBER_ID, ROOM_UUID, ROOM_ASSET_ID, request);

            // then
            assertThat(assetWithNullKey.getStorageKey()).isEqualTo(NEW_STORAGE_KEY);
            verify(storageService, never()).deleteObject(any());
        }

        @Test
        @DisplayName("성공: R2 삭제 실패해도 storageKey 갱신은 유지된다")
        void overwrite_r2DeleteFails_storageKeyStillUpdated() {
            // given
            stubRoomAndParticipant();

            RoomAsset roomAsset = RoomAsset.createFromTeamAsset(room, teamAsset, member, SavePolicy.OVERWRITE);
            given(roomAssetRepository.findByIdAndRoomUuid(ROOM_ASSET_ID, ROOM_UUID))
                    .willReturn(Optional.of(roomAsset));
            doThrow(new RuntimeException("R2 error")).when(storageService).deleteObject(OLD_STORAGE_KEY);

            AssetSaveRequest request = new AssetSaveRequest(NEW_STORAGE_KEY);

            // when
            RoomAssetResponse response = roomAssetService.saveAsset(MEMBER_ID, ROOM_UUID, ROOM_ASSET_ID, request);

            // then
            assertThat(teamAsset.getStorageKey()).isEqualTo(NEW_STORAGE_KEY);
            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("saveAsset - NEW_COPY")
    class SaveAssetNewCopy {

        @Test
        @DisplayName("성공: 새 버전 TeamAsset이 생성되고 RoomAsset의 참조가 갱신된다")
        void newCopy_success() {
            // given
            stubRoomAndParticipant();

            RoomAsset roomAsset = RoomAsset.createFromTeamAsset(room, teamAsset, member, SavePolicy.NEW_COPY);
            given(roomAssetRepository.findByIdAndRoomUuid(ROOM_ASSET_ID, ROOM_UUID))
                    .willReturn(Optional.of(roomAsset));
            given(teamAssetRepository.save(any(TeamAsset.class))).willAnswer(invocation -> invocation.getArgument(0));

            AssetSaveRequest request = new AssetSaveRequest(NEW_STORAGE_KEY);

            // when
            RoomAssetResponse response = roomAssetService.saveAsset(MEMBER_ID, ROOM_UUID, ROOM_ASSET_ID, request);

            // then
            assertThat(teamAsset.getIsLatest()).isFalse();
            verify(teamAssetRepository).save(any(TeamAsset.class));
            verify(storageService, never()).deleteObject(any());
            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("saveAsset - 예외 케이스")
    class SaveAssetExceptions {

        @Test
        @DisplayName("실패: 존재하지 않는 RoomAsset이면 예외가 발생한다")
        void roomAssetNotFound() {
            // given
            stubRoomAndParticipant();
            given(roomAssetRepository.findByIdAndRoomUuid(ROOM_ASSET_ID, ROOM_UUID))
                    .willReturn(Optional.empty());

            AssetSaveRequest request = new AssetSaveRequest(NEW_STORAGE_KEY);

            // when & then
            assertThatThrownBy(() -> roomAssetService.saveAsset(MEMBER_ID, ROOM_UUID, ROOM_ASSET_ID, request))
                    .isInstanceOf(MeetingException.class);
        }

        @Test
        @DisplayName("실패: 삭제된 TeamAsset이면 예외가 발생한다")
        void deletedTeamAsset() {
            // given
            stubRoomAndParticipant();

            teamAsset.delete();
            RoomAsset roomAsset = RoomAsset.createFromTeamAsset(room, teamAsset, member, SavePolicy.OVERWRITE);
            given(roomAssetRepository.findByIdAndRoomUuid(ROOM_ASSET_ID, ROOM_UUID))
                    .willReturn(Optional.of(roomAsset));

            AssetSaveRequest request = new AssetSaveRequest(NEW_STORAGE_KEY);

            // when & then
            assertThatThrownBy(() -> roomAssetService.saveAsset(MEMBER_ID, ROOM_UUID, ROOM_ASSET_ID, request))
                    .isInstanceOf(MeetingException.class);
        }
    }
}
