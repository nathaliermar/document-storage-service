package com.documentstorage.application.dto.request;

import java.util.UUID;

public record UploadRequest(UUID ownerId, String fileName, String contentType) {
}
