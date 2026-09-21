package com.datashare.backend.service;

import com.datashare.backend.entity.StoredFile;
import com.datashare.backend.repository.StoredFileRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ExpiredFileCleanupServiceTest {

    private StoredFileRepository
            storedFileRepository;

    private ExpiredFileCleanupService
            cleanupService;

    private Path testFile;


    @BeforeEach
    void setUp() throws Exception {

        storedFileRepository =
                mock(
                        StoredFileRepository.class
                );


        cleanupService =
                new ExpiredFileCleanupService(
                        storedFileRepository
                );


        Files.createDirectories(
                Paths.get("uploads")
        );


        testFile =
                Paths.get("uploads")
                        .resolve(
                                "cleanup-test-"
                                        + UUID.randomUUID()
                                        + ".txt"
                        );
    }


    @AfterEach
    void cleanUp() throws Exception {

        Files.deleteIfExists(
                testFile
        );
    }


    @Test
    void shouldDeleteExpiredPhysicalFileAndDatabaseEntry()
            throws Exception {

        Files.writeString(
                testFile,
                "expired file"
        );


        StoredFile storedFile =
                mock(
                        StoredFile.class
                );


        when(
                storedFile.getId()
        ).thenReturn(
                1L
        );


        when(
                storedFile.getStorageName()
        ).thenReturn(
                testFile
                        .getFileName()
                        .toString()
        );


        when(
                storedFileRepository
                        .findByExpiresAtBefore(
                                any(LocalDateTime.class)
                        )
        ).thenReturn(
                List.of(
                        storedFile
                )
        );


        cleanupService
                .deleteExpiredFiles();


        assertFalse(
                Files.exists(
                        testFile
                )
        );


        verify(
                storedFileRepository
        ).delete(
                storedFile
        );
    }


    @Test
    void shouldDoNothingWhenThereAreNoExpiredFiles() {

        when(
                storedFileRepository
                        .findByExpiresAtBefore(
                                any(LocalDateTime.class)
                        )
        ).thenReturn(
                List.of()
        );


        cleanupService
                .deleteExpiredFiles();


        verify(
                storedFileRepository,
                never()
        ).delete(
                any(StoredFile.class)
        );
    }
}