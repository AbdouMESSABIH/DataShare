package com.datashare.backend.service;

import com.datashare.backend.entity.StoredFile;
import com.datashare.backend.repository.StoredFileRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;

import java.util.List;


@Service
public class ExpiredFileCleanupService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ExpiredFileCleanupService.class
            );


    private final StoredFileRepository
            storedFileRepository;


    private final Path uploadDirectory =
            Paths.get("uploads");


    public ExpiredFileCleanupService(
            StoredFileRepository storedFileRepository
    ) {

        this.storedFileRepository =
                storedFileRepository;
    }


    @Scheduled(
            fixedDelayString =
                    "${datashare.cleanup.fixed-delay-ms:3600000}",
            initialDelayString =
                    "${datashare.cleanup.initial-delay-ms:60000}"
    )
    @Transactional
    public void deleteExpiredFiles() {

        LocalDateTime now =
                LocalDateTime.now();


        List<StoredFile> expiredFiles =
                storedFileRepository
                        .findByExpiresAtBefore(
                                now
                        );


        if (expiredFiles.isEmpty()) {

            return;
        }


        int deletedCount = 0;


        for (
                StoredFile storedFile :
                expiredFiles
        ) {

            Path filePath =
                    uploadDirectory.resolve(
                            storedFile.getStorageName()
                    );


            try {

                Files.deleteIfExists(
                        filePath
                );


                storedFileRepository.delete(
                        storedFile
                );


                deletedCount++;


                log.atInfo()
                        .addKeyValue(
                                "event",
                                "expired_file_cleanup"
                        )
                        .addKeyValue(
                                "fileId",
                                storedFile.getId()
                        )
                        .log(
                                "Expired file deleted"
                        );


            } catch (IOException e) {

                log.atWarn()
                        .addKeyValue(
                                "event",
                                "expired_file_cleanup_failed"
                        )
                        .addKeyValue(
                                "fileId",
                                storedFile.getId()
                        )
                        .setCause(e)
                        .log(
                                "Unable to delete expired file"
                        );
            }
        }


        log.atInfo()
                .addKeyValue(
                        "event",
                        "expired_files_cleanup_completed"
                )
                .addKeyValue(
                        "deletedFiles",
                        deletedCount
                )
                .log(
                        "Expired files cleanup completed"
                );
    }
}