package com.writingboard.server.global.storage;

import com.writingboard.server.global.storage.dto.PresignedDownloadRequest;
import com.writingboard.server.global.storage.dto.PresignedDownloadResponse;
import com.writingboard.server.global.storage.dto.PresignedUploadRequest;
import com.writingboard.server.global.storage.dto.PresignedUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Slf4j
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/presigned-url/upload")
    @Operation(summary = "업로드 Presigned URL 발급", description = "R2에 파일을 업로드하기 위한 Presigned URL을 발급한다.")
    public ResponseEntity<PresignedUploadResponse> getUploadPresignedUrl(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid PresignedUploadRequest request) {

        log.info("Member {} requested upload presigned URL for folder: {}, fileName: {}", memberId, request.folder(), request.fileName());
        PresignedUploadResponse response = storageService.generateUploadUrl(
                request.folder(), request.fileName(), request.contentType());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/presigned-url/download")
    @Operation(summary = "다운로드 Presigned URL 발급", description = "R2에서 파일을 다운로드하기 위한 Presigned URL을 발급한다.")
    public ResponseEntity<PresignedDownloadResponse> getDownloadPresignedUrl(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid PresignedDownloadRequest request) {

        PresignedDownloadResponse response = storageService.generateDownloadUrl(request.storageKey());
        return ResponseEntity.ok(response);
    }
}
