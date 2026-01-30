package com.writingboard.server.domain.team.controller;

import com.writingboard.server.domain.team.dto.request.AssetCreateRequest;
import com.writingboard.server.domain.team.dto.request.AssetUpdateRequest;
import com.writingboard.server.domain.team.dto.response.AssetListResponse;
import com.writingboard.server.domain.team.dto.response.AssetResponse;
import com.writingboard.server.domain.team.service.TeamAssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/assets")
@RequiredArgsConstructor
public class TeamAssetController {

    private final TeamAssetService teamAssetService;

    @PostMapping
    public ResponseEntity<AssetResponse> createAsset(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @RequestBody @Valid AssetCreateRequest request) {

        AssetResponse response = teamAssetService.createAsset(memberId, teamId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<AssetListResponse> getAssets(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        AssetListResponse response = teamAssetService.getAssets(memberId, teamId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{assetId}")
    public ResponseEntity<AssetResponse> getAsset(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long assetId) {

        AssetResponse response = teamAssetService.getAsset(memberId, teamId, assetId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{assetId}/versions")
    public ResponseEntity<List<AssetResponse>> getVersionHistory(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long assetId) {

        List<AssetResponse> response = teamAssetService.getVersionHistory(memberId, teamId, assetId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{assetId}")
    public ResponseEntity<AssetResponse> updateAsset(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long assetId,
            @RequestBody @Valid AssetUpdateRequest request) {

        AssetResponse response = teamAssetService.updateAsset(memberId, teamId, assetId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<Void> deleteAsset(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long assetId) {

        teamAssetService.deleteAsset(memberId, teamId, assetId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{assetId}/versions")
    public ResponseEntity<AssetResponse> createNewVersion(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long assetId) {

        AssetResponse response = teamAssetService.createNewVersion(memberId, teamId, assetId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
