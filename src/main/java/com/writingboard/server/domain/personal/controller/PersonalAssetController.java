package com.writingboard.server.domain.personal.controller;

import com.writingboard.server.domain.personal.dto.request.PersonalAssetCreateRequest;
import com.writingboard.server.domain.personal.dto.response.PersonalAssetResponse;
import com.writingboard.server.domain.personal.service.PersonalAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/personal-assets")
@RequiredArgsConstructor
@Tag(name = "Personal Asset", description = "개인 자료 관리 API")
public class PersonalAssetController {

    private final PersonalAssetService personalAssetService;

    @PostMapping
    @Operation(summary = "개인 자료 생성", description = "새로운 개인 자료를 생성합니다")
    public ResponseEntity<PersonalAssetResponse> createAsset(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid PersonalAssetCreateRequest request) {
        PersonalAssetResponse response = personalAssetService.createAsset(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "개인 자료 목록 조회", description = "본인의 개인 자료 목록을 조회합니다")
    public ResponseEntity<Page<PersonalAssetResponse>> getAssets(
            @AuthenticationPrincipal Long memberId,
            Pageable pageable) {
        return ResponseEntity.ok(personalAssetService.getAssets(memberId, pageable));
    }

    @GetMapping("/{assetId}")
    @Operation(summary = "개인 자료 상세 조회", description = "개인 자료의 상세 정보를 조회합니다")
    public ResponseEntity<PersonalAssetResponse> getAsset(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long assetId) {
        return ResponseEntity.ok(personalAssetService.getAsset(memberId, assetId));
    }

    @DeleteMapping("/{assetId}")
    @Operation(summary = "개인 자료 삭제", description = "개인 자료를 삭제합니다")
    public ResponseEntity<Void> deleteAsset(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long assetId) {
        personalAssetService.deleteAsset(memberId, assetId);
        return ResponseEntity.noContent().build();
    }
}
