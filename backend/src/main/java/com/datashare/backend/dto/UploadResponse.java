package com.datashare.backend.dto;

import java.time.LocalDateTime;

public class UploadResponse {

    private final Long id;
    private final String originalName;
    private final Long size;
    private final String downloadToken;
    private final LocalDateTime expiresAt;

    public UploadResponse(
            Long id,
            String originalName,
            Long size,
            String downloadToken,
            LocalDateTime expiresAt
    ) {
        this.id = id;
        this.originalName = originalName;
        this.size = size;
        this.downloadToken = downloadToken;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public Long getSize() {
        return size;
    }

    public String getDownloadToken() {
        return downloadToken;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}