package com.datashare.backend.service;

import com.datashare.backend.entity.StoredFile;
import com.datashare.backend.repository.StoredFileRepository;
import com.datashare.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class FileServiceTest {

    private StoredFileRepository
            storedFileRepository;

    private UserRepository
            userRepository;

    private FileService
            fileService;


    @BeforeEach
    void setUp() {

        storedFileRepository =
                mock(
                        StoredFileRepository.class
                );

        userRepository =
                mock(
                        UserRepository.class
                );

        fileService =
                new FileService(
                        storedFileRepository,
                        userRepository
                );
    }


    @Test
    void uploadShouldRejectExeFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.exe",
                        "application/octet-stream",
                        "fake executable content"
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                fileService.upload(
                                        file,
                                        "test@test.com",
                                        7
                                )
                );


        assertEquals(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                exception.getStatusCode()
        );


        verifyNoInteractions(
                storedFileRepository,
                userRepository
        );
    }


    @Test
    void uploadShouldRejectUnknownExtension() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "archive.zip",
                        "application/zip",
                        new byte[]{
                                0x50,
                                0x4B,
                                0x03,
                                0x04
                        }
                );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                fileService.upload(
                                        file,
                                        "test@test.com",
                                        7
                                )
                );


        assertEquals(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                exception.getStatusCode()
        );


        verifyNoInteractions(
                storedFileRepository,
                userRepository
        );
    }


    @Test
    void uploadShouldRejectFakePdf() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "fake.pdf",
                        "application/pdf",
                        "Ceci n'est pas un vrai PDF"
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                fileService.upload(
                                        file,
                                        "test@test.com",
                                        7
                                )
                );


        assertEquals(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                exception.getStatusCode()
        );


        verifyNoInteractions(
                storedFileRepository,
                userRepository
        );
    }


    @Test
    void uploadShouldRejectPdfDisguisedAsText() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "document.txt",
                        "text/plain",
                        "%PDF-1.7 fake pdf"
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                fileService.upload(
                                        file,
                                        "test@test.com",
                                        7
                                )
                );


        assertEquals(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                exception.getStatusCode()
        );


        verifyNoInteractions(
                storedFileRepository,
                userRepository
        );
    }


    @Test
    void uploadShouldRejectEmptyFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "empty.txt",
                        "text/plain",
                        new byte[0]
                );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                fileService.upload(
                                        file,
                                        "test@test.com",
                                        7
                                )
                );


        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );


        verifyNoInteractions(
                storedFileRepository,
                userRepository
        );
    }


    @Test
    void uploadShouldRejectExpirationGreaterThanSevenDays() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.txt",
                        "text/plain",
                        "contenu"
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                fileService.upload(
                                        file,
                                        "test@test.com",
                                        8
                                )
                );


        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );


        verifyNoInteractions(
                storedFileRepository,
                userRepository
        );
    }


    @Test
    void uploadShouldRejectFileLargerThanOneGb() {

        MultipartFile file =
                mock(
                        MultipartFile.class
                );


        when(
                file.isEmpty()
        ).thenReturn(
                false
        );


        when(
                file.getSize()
        ).thenReturn(
                1024L
                        * 1024
                        * 1024
                        + 1
        );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                fileService.upload(
                                        file,
                                        "test@test.com",
                                        7
                                )
                );


        assertEquals(
                HttpStatus.PAYLOAD_TOO_LARGE,
                exception.getStatusCode()
        );


        verifyNoInteractions(
                storedFileRepository,
                userRepository
        );
    }


    @Test
    void getByDownloadTokenShouldRejectUnknownToken() {

        when(
                storedFileRepository
                        .findByDownloadToken(
                                "invalid-token"
                        )
        ).thenReturn(
                Optional.empty()
        );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                fileService
                                        .getByDownloadToken(
                                                "invalid-token"
                                        )
                );


        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }


    @Test
    void getByDownloadTokenShouldRejectExpiredToken() {

        StoredFile storedFile =
                mock(
                        StoredFile.class
                );


        when(
                storedFileRepository
                        .findByDownloadToken(
                                "expired-token"
                        )
        ).thenReturn(
                Optional.of(
                        storedFile
                )
        );


        when(
                storedFile.getExpiresAt()
        ).thenReturn(
                LocalDateTime.now()
                        .minusDays(1)
        );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                fileService
                                        .getByDownloadToken(
                                                "expired-token"
                                        )
                );


        assertEquals(
                HttpStatus.GONE,
                exception.getStatusCode()
        );
    }
}