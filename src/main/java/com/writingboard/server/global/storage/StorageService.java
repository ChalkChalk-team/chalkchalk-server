package com.writingboard.server.global.storage;

import com.writingboard.server.global.storage.dto.PresignedDownloadResponse;
import com.writingboard.server.global.storage.dto.PresignedUploadResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private static final Duration UPLOAD_URL_EXPIRATION = Duration.ofMinutes(10);
    private static final Duration DOWNLOAD_URL_EXPIRATION = Duration.ofMinutes(60);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final R2Properties r2Properties;

    public PresignedUploadResponse generateUploadUrl(Long teamId, String fileName, String contentType) {
        String storageKey = "teams/" + teamId + "/" + UUID.randomUUID() + "_" + fileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(r2Properties.getBucket())
                .key(storageKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(UPLOAD_URL_EXPIRATION)
                .putObjectRequest(putObjectRequest)
                .build();

        String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

        log.info("Presigned upload URL generated for key: {}", storageKey);
        return new PresignedUploadResponse(uploadUrl, storageKey);
    }

    public PresignedDownloadResponse generateDownloadUrl(String storageKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(r2Properties.getBucket())
                .key(storageKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(DOWNLOAD_URL_EXPIRATION)
                .getObjectRequest(getObjectRequest)
                .build();

        String downloadUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

        log.info("Presigned download URL generated for key: {}", storageKey);
        return new PresignedDownloadResponse(downloadUrl);
    }

    public String uploadObject(String storageKey, InputStream inputStream, long contentLength, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(r2Properties.getBucket())
                .key(storageKey)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength)); // 서버에서 직접 업로드

        return storageKey;
    }

    public void deleteObject(String storageKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(r2Properties.getBucket())
                .key(storageKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        log.info("Object deleted from R2: {}", storageKey);
    }
}
