package com.documentstorage.application.dto.response;

import java.time.Instant;

public record PresignerUrlResponse(String url, Instant expiresAt) {
}
