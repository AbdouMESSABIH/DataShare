package com.datashare.backend.repository;

import com.datashare.backend.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
}