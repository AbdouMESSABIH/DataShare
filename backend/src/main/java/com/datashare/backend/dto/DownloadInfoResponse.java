package com.datashare.backend.dto;

import java.time.LocalDateTime;

public class DownloadInfoResponse {

    private final String originalName;
    private final Long size;
    private final String contentType;
    private final LocalDateTime expiresAt;
    private final boolean passwordProtected;

    public DownloadInfoResponse(
            String originalName,
            Long size,
            String contentType,
            LocalDateTime expiresAt,
            boolean passwordProtected
    ) {
        this.originalName = originalName;
        this.size = size;
        this.contentType = contentType;
        this.expiresAt = expiresAt;
        this.passwordProtected = passwordProtected;
    }

    public String getOriginalName() { return originalName; }

    public Long getSize() { return size; }

    public String getContentType() { return contentType; }

    public LocalDateTime getExpiresAt() { return expiresAt; }

    public boolean isPasswordProtected() {
        return passwordProtected;
    }
    
}