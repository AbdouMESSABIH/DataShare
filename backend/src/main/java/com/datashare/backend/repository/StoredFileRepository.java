package com.datashare.backend.repository;

import com.datashare.backend.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {

    Optional<StoredFile> findByDownloadToken(String downloadToken);

    List<StoredFile> findByOwnerEmailOrderByCreatedAtDesc(String email);

    Optional<StoredFile> findByIdAndOwnerEmail(Long id, String email);
}