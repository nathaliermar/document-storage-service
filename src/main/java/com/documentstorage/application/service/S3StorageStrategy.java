package com.documentstorage.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;

@Service
public class S3StorageStrategy implements StorageStrategy {

    private final S3Client s3SClient;
    private final S3Presigner presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3StorageStrategy(S3Client s3SClient, S3Presigner presigner) {
        this.s3SClient = s3SClient;
        this.presigner = presigner;
    }

    @Override
    public String upload(String key, InputStream inputStream, long fileSize, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength(fileSize)
                .build();

        s3SClient.putObject(request, RequestBody.fromInputStream(inputStream, fileSize));
        return key;
    }

    @Override
    public String generatePresignedUrl(String key, Duration expiration) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(r -> r.bucket(bucketName).key(key))
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void delete(String key) {
        s3SClient.deleteObject(r -> r.bucket(bucketName).key(key));
    }
}
