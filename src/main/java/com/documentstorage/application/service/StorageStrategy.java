package com.documentstorage.application.service;

import java.io.InputStream;
import java.time.Duration;

public interface StorageStrategy {

    String upload(String key, InputStream inputStream, long fileSize, String contentType);
    String generatePresignedUrl(String key, Duration expiration);
    void delete(String key);
}
