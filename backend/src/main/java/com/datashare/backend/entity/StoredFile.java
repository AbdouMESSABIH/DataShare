package com.datashare.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class StoredFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "storage_name", nullable = false, unique = true)
    private String storageName;

    @Column(name = "content_type")
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(name = "download_token", nullable = false, unique = true)
    private String downloadToken;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    public StoredFile() {
    }

    public StoredFile(
            String originalName,
            String storageName,
            String contentType,
            Long size,
            String downloadToken,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            User owner
    ) {
        this.originalName = originalName;
        this.storageName = storageName;
        this.contentType = contentType;
        this.size = size;
        this.downloadToken = downloadToken;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getStorageName() {
        return storageName;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSize() {
        return size;
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

    public User getOwner() {
        return owner;
    }
}