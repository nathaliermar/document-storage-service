package com.documentstorage.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String fileName,
        String contentType,
        Long fileSize,
        LocalDateTime uploadedAt
) {}