package com.writingboard.server.domain.team.service;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.domain.team.dto.TeamAssetPreviewImageUpdateResponse;
import com.writingboard.server.domain.team.dto.request.AssetCreateRequest;
import com.writingboard.server.domain.team.dto.request.AssetNewVersionRequest;
import com.writingboard.server.domain.team.dto.request.AssetUpdateRequest;
import com.writingboard.server.domain.team.dto.response.AssetListResponse;
import com.writingboard.server.domain.team.dto.response.AssetResponse;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamAssetService {

    private final TeamAssetRepository teamAssetRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;
    private final StorageService storageService;

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

    @Transactional
    public TeamAssetPreviewImageUpdateResponse updatePreviewImage(Long memberId, Long teamId, Long assetId, MultipartFile file) {
        validateTeamMember(teamId, memberId);

        TeamAsset teamAsset = teamAssetRepository.findByIdAndTeamIdAndStatus(assetId, teamId, AssetStatus.ACTIVE)
                .orElseThrow(() -> new TeamException(TeamErrorCode.ASSET_NOT_FOUND));

        // 고정 키 패턴으로 동일 위치에 덮어씀
        String previewStorageKey = "previews/teams/" + teamId + "/assets/" + assetId + ".jpg";

        try {
            storageService.uploadObject(previewStorageKey, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new TeamException(TeamErrorCode.PREVIEW_UPLOAD_FAILED);
        }

        teamAsset.updatePreviewImageUrl(previewStorageKey);

        String previewUrl = storageService.generateDownloadUrl(previewStorageKey).downloadUrl();
        return TeamAssetPreviewImageUpdateResponse.of(assetId, previewUrl);
    }

    // Helper methods
    private AssetResponse buildAssetResponse(TeamAsset asset) {
        AssetResponse response = AssetResponse.of(asset);
        if (asset.getPreviewImageUrl() != null) {
            String previewUrl = storageService.generateDownloadUrl(asset.getPreviewImageUrl()).downloadUrl();
            response.setThumbnailImageUrl(previewUrl);
        }
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
