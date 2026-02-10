package com.writingboard.server.global.storage;

import com.writingboard.server.domain.personal.entity.PersonalAsset;
import com.writingboard.server.domain.personal.repository.PersonalAssetRepository;
import com.writingboard.server.domain.team.entity.TeamAsset;
import com.writingboard.server.domain.team.entity.enums.AssetSourceType;
import com.writingboard.server.domain.team.entity.enums.AssetStatus;
import com.writingboard.server.domain.team.entity.enums.AssetType;
import com.writingboard.server.domain.team.repository.TeamAssetRepository;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.team.entity.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageCleanupSchedulerTest {

    @InjectMocks
    private StorageCleanupScheduler scheduler;

    @Mock private TeamAssetRepository teamAssetRepository;
    @Mock private PersonalAssetRepository personalAssetRepository;
    @Mock private StorageService storageService;

    @Test
    @DisplayName("만료된 팀 자산의 R2 파일을 삭제하고 storageKey를 null로 변경한다")
    void cleanupTeamAssets() {
        // given
        Team team = mock(Team.class);
        Member member = mock(Member.class);

        TeamAsset asset1 = TeamAsset.create(team, member, AssetType.PDF, "file1.pdf",
                AssetSourceType.UPLOADED, "teams/1/file1.pdf", 3);
        asset1.delete(); // sets status=DELETED, deletedAt=now

        TeamAsset asset2 = TeamAsset.create(team, member, AssetType.PDF, "file2.pdf",
                AssetSourceType.UPLOADED, "teams/1/file2.pdf", 5);
        asset2.delete();

        given(teamAssetRepository.findByStatusAndDeletedAtBeforeAndStorageKeyIsNotNull(
                eq(AssetStatus.DELETED), any(Instant.class)))
                .willReturn(List.of(asset1, asset2));
        given(personalAssetRepository.findByDeletedAtBeforeAndStorageKeyIsNotNull(any(Instant.class)))
                .willReturn(List.of());

        // when
        scheduler.cleanupExpiredAssets();

        // then
        verify(storageService).deleteObject("teams/1/file1.pdf");
        verify(storageService).deleteObject("teams/1/file2.pdf");
        assertThat(asset1.getStorageKey()).isNull();
        assertThat(asset2.getStorageKey()).isNull();
    }

    @Test
    @DisplayName("R2 삭제 실패 시 해당 자산은 건너뛰고 나머지를 계속 처리한다")
    void cleanupContinuesOnFailure() {
        // given
        Team team = mock(Team.class);
        Member member = mock(Member.class);

        TeamAsset failAsset = TeamAsset.create(team, member, AssetType.PDF, "fail.pdf",
                AssetSourceType.UPLOADED, "teams/1/fail.pdf", 1);
        failAsset.delete();

        TeamAsset successAsset = TeamAsset.create(team, member, AssetType.PDF, "success.pdf",
                AssetSourceType.UPLOADED, "teams/1/success.pdf", 1);
        successAsset.delete();

        given(teamAssetRepository.findByStatusAndDeletedAtBeforeAndStorageKeyIsNotNull(
                eq(AssetStatus.DELETED), any(Instant.class)))
                .willReturn(List.of(failAsset, successAsset));
        given(personalAssetRepository.findByDeletedAtBeforeAndStorageKeyIsNotNull(any(Instant.class)))
                .willReturn(List.of());

        doThrow(new RuntimeException("R2 error")).when(storageService).deleteObject("teams/1/fail.pdf");

        // when
        scheduler.cleanupExpiredAssets();

        // then
        assertThat(failAsset.getStorageKey()).isEqualTo("teams/1/fail.pdf"); // 실패한 건 유지
        assertThat(successAsset.getStorageKey()).isNull(); // 성공한 건 정리됨
        verify(storageService, times(2)).deleteObject(any());
    }

    @Test
    @DisplayName("만료된 자산이 없으면 R2 삭제를 호출하지 않는다")
    void noExpiredAssets() {
        // given
        given(teamAssetRepository.findByStatusAndDeletedAtBeforeAndStorageKeyIsNotNull(
                eq(AssetStatus.DELETED), any(Instant.class)))
                .willReturn(List.of());
        given(personalAssetRepository.findByDeletedAtBeforeAndStorageKeyIsNotNull(any(Instant.class)))
                .willReturn(List.of());

        // when
        scheduler.cleanupExpiredAssets();

        // then
        verify(storageService, never()).deleteObject(any());
    }

    @Test
    @DisplayName("만료된 개인 자산의 R2 파일도 정리한다")
    void cleanupPersonalAssets() {
        // given
        Member member = mock(Member.class);

        PersonalAsset personalAsset = PersonalAsset.create(member, "personal.pdf",
                AssetType.PDF, "personal/1/file.pdf", 1024L, 2);
        personalAsset.softDelete();

        given(teamAssetRepository.findByStatusAndDeletedAtBeforeAndStorageKeyIsNotNull(
                eq(AssetStatus.DELETED), any(Instant.class)))
                .willReturn(List.of());
        given(personalAssetRepository.findByDeletedAtBeforeAndStorageKeyIsNotNull(any(Instant.class)))
                .willReturn(List.of(personalAsset));

        // when
        scheduler.cleanupExpiredAssets();

        // then
        verify(storageService).deleteObject("personal/1/file.pdf");
        assertThat(personalAsset.getStorageKey()).isNull();
    }
}
