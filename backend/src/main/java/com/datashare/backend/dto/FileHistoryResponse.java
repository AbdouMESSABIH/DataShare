package com.datashare.backend.dto;

import java.time.LocalDateTime;

public class FileHistoryResponse {

    private final Long id;
    private final String originalName;
    private final Long size;
    private final String contentType;
    private final String downloadToken;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final boolean passwordProtected;

    public FileHistoryResponse(
            Long id,
            String originalName,
            Long size,
            String contentType,
            String downloadToken,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            boolean passwordProtected
    ) {
        this.id = id;
        this.originalName = originalName;
        this.size = size;
        this.contentType = contentType;
        this.downloadToken = downloadToken;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.passwordProtected = passwordProtected;
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

    public String getContentType() {
        return contentType;
    }

    public String getDownloadToken() {
        return downloadToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isPasswordProtected() {
        return passwordProtected;
    }
}