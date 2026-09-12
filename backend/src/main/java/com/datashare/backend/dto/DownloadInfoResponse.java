package com.datashare.backend.dto;

import java.time.LocalDateTime;

public class DownloadInfoResponse {

    private final String originalName;
    private final Long size;
    private final String contentType;
    private final LocalDateTime expiresAt;

    public DownloadInfoResponse(
            String originalName,
            Long size,
            String contentType,
            LocalDateTime expiresAt
    ) {
        this.originalName = originalName;
        this.size = size;
        this.contentType = contentType;
        this.expiresAt = expiresAt;
    }

    public String getOriginalName() { return originalName; }

    public Long getSize() { return size; }

    public String getContentType() { return contentType; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    
}