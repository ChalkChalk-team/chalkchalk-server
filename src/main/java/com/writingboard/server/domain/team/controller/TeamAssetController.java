package com.writingboard.server.domain.team.controller;

import com.writingboard.server.domain.drawing.dto.response.DrawingSnapshotResponse;
import com.writingboard.server.domain.team.dto.TeamAssetPreviewImageUpdateResponse;
import com.writingboard.server.domain.team.dto.request.AssetCreateRequest;
import com.writingboard.server.domain.team.dto.request.AssetNewVersionRequest;
import com.writingboard.server.domain.team.dto.request.AssetUpdateRequest;
import com.writingboard.server.domain.team.dto.request.TeamAssetDrawingUpdateRequest;
import com.writingboard.server.domain.team.dto.response.AssetListResponse;
import com.writingboard.server.domain.team.dto.response.AssetResponse;
import com.writingboard.server.domain.team.dto.response.TeamAssetNoteDataResponse;
import com.writingboard.server.domain.team.service.TeamAssetService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/assets")
@RequiredArgsConstructor
public class TeamAssetController {

    private final TeamAssetService teamAssetService;

    @PostMapping
    @Operation(summary = "팀 파일 저장", description = "팀 자료실 내에 파일을 저장한다.")
    public ResponseEntity<AssetResponse> createAsset(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @RequestBody @Valid AssetCreateRequest request) {

        AssetResponse response = teamAssetService.createAsset(memberId, teamId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "팀 파일 목록 조회", description = "팀 자료실 내에 저장된 파일들의 목록을 조회한다.")
    public ResponseEntity<AssetListResponse> getAssets(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        AssetListResponse response = teamAssetService.getAssets(memberId, teamId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{assetId}")
    @Operation(summary = "팀 파일 상세 조회", description = "팀 자료실 내에 저장된 특정 파일의 상세 정보를 조회한다.")
    public ResponseEntity<AssetResponse> getAsset(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long assetId) {

        AssetResponse response = teamAssetService.getAsset(memberId, teamId, assetId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{assetId}/versions")
    @Operation(summary = "팀 파일 버전 히스토리 조회", description = "팀 자료실 내에 저장된 특정 파일의 버전 히스토리를 조회한다.")
    public ResponseEntity<List<AssetResponse>> getVersionHistory(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long assetId) {

        List<AssetResponse> response = teamAssetService.getVersionHistory(memberId, teamId, assetId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{assetId}")
    @Operation(summary = "팀 파일 정보 수정", description = "팀 자료실 내에 저장된 특정 파일의 정보를 수정한다.")
    public ResponseEntity<AssetResponse> updateAsset(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long assetId,
            @RequestBody @Valid AssetUpdateRequest request) {

        AssetResponse response = teamAssetService.updateAsset(memberId, teamId, assetId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{assetId}")
    @Operation(summary = "팀 파일 삭제", description = "팀 자료실 내에 저장된 특정 파일을 삭제(Soft delete)한다.(OWNER, ADMIN 권한 필요)")
    public ResponseEntity<Void> deleteAsset(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long assetId) {

        teamAssetService.deleteAsset(memberId, teamId, assetId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{assetId}/versions")
    @Operation(summary = "팀 파일 새 버전 생성", description = "팀 자료실 내에 저장된 특정 파일의 새 버전을 생성한다.")
    public ResponseEntity<AssetResponse> createNewVersion(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long assetId,
            @RequestBody @Valid AssetNewVersionRequest request) {

        AssetResponse response = teamAssetService.createNewVersion(memberId, teamId, assetId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{assetId}/note-data")
    @Operation(summary = "팀 노트 데이터 조회", description = "팀 노트의 PDF 다운로드 URL과 회의에서 작성한 페이지별 필기 데이터를 함께 반환한다.")
    public ResponseEntity<TeamAssetNoteDataResponse> getNoteData(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long assetId) {

        TeamAssetNoteDataResponse response = teamAssetService.getNoteData(memberId, teamId, assetId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{assetId}/drawing-data")
    @Operation(summary = "팀 노트 필기 데이터 저장", description = "회의 종료 후 팀 노트의 특정 페이지 필기 데이터를 저장한다.")
    public ResponseEntity<DrawingSnapshotResponse> saveDrawingData(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long assetId,
            @RequestBody @Valid TeamAssetDrawingUpdateRequest request) {

        DrawingSnapshotResponse response = teamAssetService.saveDrawingData(memberId, teamId, assetId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(value = "/{assetId}/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "팀 노트 preview 이미지 업데이트", description = "회의실에서 필기 후 팀 노트 목록에 보여줄 thumbnail 이미지를 업데이트한다.")
    public ResponseEntity<TeamAssetPreviewImageUpdateResponse> updatePreviewImage(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long assetId,
            @RequestPart("previewImage") MultipartFile previewImage) {

        TeamAssetPreviewImageUpdateResponse response = teamAssetService.updatePreviewImage(memberId, teamId, assetId, previewImage);
        return ResponseEntity.ok(response);
    }
}
