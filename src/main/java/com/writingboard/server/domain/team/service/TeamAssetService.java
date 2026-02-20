package com.writingboard.server.domain.team.service;

import com.writingboard.server.domain.drawing.document.DrawingSnapshot;
import com.writingboard.server.domain.drawing.repository.DrawingSnapshotRepository;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.domain.meeting.entity.RoomAsset;
import com.writingboard.server.domain.meeting.repository.RoomAssetRepository;
import com.writingboard.server.domain.team.dto.TeamAssetPreviewImageUpdateResponse;
import com.writingboard.server.domain.team.dto.request.AssetCreateRequest;
import com.writingboard.server.domain.team.dto.request.AssetNewVersionRequest;
import com.writingboard.server.domain.team.dto.request.AssetUpdateRequest;
import com.writingboard.server.domain.team.dto.request.TeamAssetDrawingUpdateRequest;
import com.writingboard.server.domain.drawing.dto.response.DrawingSnapshotResponse;
import com.writingboard.server.domain.team.dto.response.AssetListResponse;
import com.writingboard.server.domain.team.dto.response.AssetResponse;
import com.writingboard.server.domain.team.dto.response.TeamAssetNoteDataResponse;
import com.writingboard.server.domain.team.entity.Team;
import com.writingboard.server.domain.team.entity.TeamAsset;
import com.writingboard.server.domain.team.entity.enums.AssetSourceType;
import com.writingboard.server.domain.team.entity.enums.AssetStatus;
import com.writingboard.server.domain.team.entity.enums.TeamMemberStatus;
import com.writingboard.server.domain.team.entity.enums.TeamRole;
import com.writingboard.server.domain.team.exception.TeamErrorCode;
import com.writingboard.server.domain.team.exception.TeamException;
import com.writingboard.server.domain.team.repository.TeamAssetRepository;
import com.writingboard.server.domain.team.repository.TeamMemberRepository;
import com.writingboard.server.domain.team.repository.TeamRepository;
import com.writingboard.server.global.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamAssetService {

    private final TeamAssetRepository teamAssetRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;
    private final StorageService storageService;
    private final RoomAssetRepository roomAssetRepository;
    private final DrawingSnapshotRepository drawingSnapshotRepository;

    @Transactional
    public AssetResponse createAsset(Long memberId, Long teamId, AssetCreateRequest request) {
        Team team = getTeamById(teamId);
        Member uploader = getMemberById(memberId);
        validateTeamMember(teamId, memberId);

        TeamAsset asset;
        if (request.getSourceType() == AssetSourceType.IMPORTED && request.getOriginPersonalAssetId() != null) {
            asset = TeamAsset.createImported(team, uploader, request.getType(), request.getName(),
                    request.getOriginPersonalAssetId(), request.getStorageKey(), request.getTotalPages());
        } else {
            asset = TeamAsset.create(team, uploader, request.getType(), request.getName(),
                    AssetSourceType.UPLOADED, request.getStorageKey(), request.getTotalPages());
        }

        teamAssetRepository.save(asset);
        return buildAssetResponse(asset);
    }

    public AssetListResponse getAssets(Long memberId, Long teamId, Pageable pageable) {
        validateTeamMember(teamId, memberId);

        Page<TeamAsset> assetPage = teamAssetRepository.findByTeamIdAndStatusAndIsLatestTrue(
                teamId, AssetStatus.ACTIVE, pageable);

        Page<AssetResponse> responsePage = assetPage.map(this::buildAssetResponse);
        return AssetListResponse.of(responsePage);
    }

    public AssetResponse getAsset(Long memberId, Long teamId, Long assetId) {
        validateTeamMember(teamId, memberId);

        TeamAsset asset = getActiveAssetByTeam(assetId, teamId);
        return buildAssetResponse(asset);
    }

    public List<AssetResponse> getVersionHistory(Long memberId, Long teamId, Long assetId) {
        validateTeamMember(teamId, memberId);

        TeamAsset asset = getActiveAssetByTeam(assetId, teamId);

        Long rootAssetId = asset.getParentAsset() != null ? asset.getParentAsset().getId() : asset.getId();
        List<TeamAsset> versions = teamAssetRepository.findVersionHistory(rootAssetId, AssetStatus.ACTIVE);

        return versions.stream()
                .map(this::buildAssetResponse)
                .toList();
    }

    @Transactional
    public AssetResponse updateAsset(Long memberId, Long teamId, Long assetId, AssetUpdateRequest request) {
        validateTeamMember(teamId, memberId);

        TeamAsset asset = getActiveAssetByTeam(assetId, teamId);
        validateModifyPermission(teamId, memberId, asset);

        asset.updateName(request.getName());
        return buildAssetResponse(asset);
    }

    @Transactional
    public void deleteAsset(Long memberId, Long teamId, Long assetId) {
        validateTeamMember(teamId, memberId);

        TeamAsset asset = getActiveAssetByTeam(assetId, teamId);
        validateModifyPermission(teamId, memberId, asset);

        if (asset.isDeleted()) {
            throw new TeamException(TeamErrorCode.ASSET_ALREADY_DELETED);
        }

        asset.delete();
    }

    @Transactional
    public AssetResponse createNewVersion(Long memberId, Long teamId, Long assetId, AssetNewVersionRequest request) {
        validateTeamMember(teamId, memberId);

        TeamAsset existingAsset = getActiveAssetByTeam(assetId, teamId);
        Member uploader = getMemberById(memberId);

        existingAsset.markAsNotLatest();

        TeamAsset newVersion = existingAsset.createNewVersion(uploader, request.getStorageKey());
        teamAssetRepository.save(newVersion);

        return buildAssetResponse(newVersion);
    }

    public TeamAssetPreviewImageUpdateResponse updatePreviewImage(Long memberId, Long teamId, Long assetId, MultipartFile file) {
        validateTeamMember(teamId, memberId);

        if (!teamAssetRepository.existsByIdAndTeamIdAndStatus(assetId, teamId, AssetStatus.ACTIVE)) {
            throw new TeamException(TeamErrorCode.ASSET_NOT_FOUND);
        }

        // teamId + assetId로 항상 동일 키 조합 → DB 저장 불필요
        String previewStorageKey = "previews/teams/" + teamId + "/assets/" + assetId + ".jpg";

        try {
            storageService.uploadObject(previewStorageKey, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new TeamException(TeamErrorCode.PREVIEW_UPLOAD_FAILED);
        }

        String previewUrl = storageService.generateDownloadUrl(previewStorageKey).downloadUrl();
        return TeamAssetPreviewImageUpdateResponse.of(assetId, previewUrl);
    }

    public TeamAssetNoteDataResponse getNoteData(Long memberId, Long teamId, Long assetId) {
        validateTeamMember(teamId, memberId);

        TeamAsset asset = getActiveAssetByTeam(assetId, teamId);

        String pdfDownloadUrl = storageService.generateDownloadUrl(asset.getStorageKey()).downloadUrl();

        Optional<RoomAsset> latestRoomAsset = roomAssetRepository.findFirstByTeamAssetIdOrderByCreatedAtDesc(assetId);

        List<TeamAssetNoteDataResponse.DrawingPageData> drawingData = latestRoomAsset
                .map(roomAsset -> {
                    List<DrawingSnapshot> snapshots = drawingSnapshotRepository.findByRoomAssetId(roomAsset.getId());
                    return snapshots.stream()
                            .collect(Collectors.toMap(
                                    DrawingSnapshot::getPageIndex,
                                    s -> s,
                                    (a, b) -> a.getVersion() >= b.getVersion() ? a : b
                            ))
                            .values().stream()
                            .sorted(Comparator.comparingInt(DrawingSnapshot::getPageIndex))
                            .map(TeamAssetNoteDataResponse.DrawingPageData::from)
                            .toList();
                })
                .orElse(List.of());

        return TeamAssetNoteDataResponse.of(asset, pdfDownloadUrl, drawingData);
    }

    @Transactional
    public DrawingSnapshotResponse saveDrawingData(Long memberId, Long teamId, Long assetId,
                                                   TeamAssetDrawingUpdateRequest request) {
        validateTeamMember(teamId, memberId);
        getActiveAssetByTeam(assetId, teamId);

        RoomAsset roomAsset = roomAssetRepository.findFirstByTeamAssetIdOrderByCreatedAtDesc(assetId)
                .orElseThrow(() -> new TeamException(TeamErrorCode.NO_MEETING_HISTORY));

        Member member = getMemberById(memberId);

        long nextVersion = drawingSnapshotRepository
                .findFirstByRoomAssetIdAndPageIndexOrderByVersionDesc(roomAsset.getId(), request.getPageIndex())
                .map(s -> s.getVersion() + 1)
                .orElse(1L);

        DrawingSnapshot snapshot = DrawingSnapshot.create(
                roomAsset.getRoom().getRoomUuid(),
                roomAsset.getId(),
                request.getPageIndex(),
                nextVersion,
                request.getSnapshotData(),
                member.getId(),
                member.getName()
        );

        return DrawingSnapshotResponse.from(drawingSnapshotRepository.save(snapshot));
    }

    // Helper methods
    private AssetResponse buildAssetResponse(TeamAsset asset) {
        AssetResponse response = AssetResponse.of(asset);
        String previewKey = "previews/teams/" + asset.getTeam().getId() + "/assets/" + asset.getId() + ".jpg";
        response.setThumbnailImageUrl(storageService.generateDownloadUrl(previewKey).downloadUrl());
        return response;
    }

    private Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamException(TeamErrorCode.TEAM_NOT_FOUND));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new TeamException(TeamErrorCode.MEMBER_NOT_FOUND));
    }

    private TeamAsset getActiveAssetByTeam(Long assetId, Long teamId) {
        return teamAssetRepository.findByIdAndTeamIdAndStatus(assetId, teamId, AssetStatus.ACTIVE)
                .orElseThrow(() -> new TeamException(TeamErrorCode.ASSET_NOT_FOUND));
    }

    private void validateTeamMember(Long teamId, Long memberId) {
        boolean isMember = teamMemberRepository.existsByTeamIdAndMemberIdAndStatus(teamId, memberId, TeamMemberStatus.ACTIVE);
        if (!isMember) {
            throw new TeamException(TeamErrorCode.NOT_TEAM_MEMBER);
        }
    }

    private void validateModifyPermission(Long teamId, Long memberId, TeamAsset asset) {
        if (asset.isUploader(memberId)) {
            return;
        }

        boolean isAdminOrOwner = teamMemberRepository.existsByTeamIdAndMemberIdAndStatusAndRoleIn(
                teamId, memberId, TeamMemberStatus.ACTIVE, List.of(TeamRole.OWNER, TeamRole.ADMIN));

        if (!isAdminOrOwner) {
            throw new TeamException(TeamErrorCode.ASSET_MODIFY_FORBIDDEN);
        }
    }
}
