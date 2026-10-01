package com.datashare.backend.controller;

import com.datashare.backend.dto.DownloadInfoResponse;
import com.datashare.backend.entity.StoredFile;
import com.datashare.backend.entity.User;
import com.datashare.backend.service.FileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.core.io.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;


class DownloadControllerTest {

    private FileService
            fileService;

    private DownloadController
            downloadController;


    @TempDir
    Path tempDirectory;


    @BeforeEach
    void setUp() {

        fileService =
                mock(
                        FileService.class
                );


        downloadController =
                new DownloadController(
                        fileService
                );
    }


    @Test
    void shouldReturnDownloadInformation() {

        StoredFile storedFile =
                createStoredFile(
                        "application/pdf"
                );


        when(
                fileService
                        .getByDownloadToken(
                                "valid-token"
                        )
        )
                .thenReturn(
                        storedFile
                );


        ResponseEntity<DownloadInfoResponse>
                response =
                downloadController
                        .getDownloadInfo(
                                "valid-token"
                        );


        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );


        assertNotNull(
                response.getBody()
        );


        assertEquals(
                "document.pdf",
                response
                        .getBody()
                        .getOriginalName()
        );


        assertEquals(
                12L,
                response
                        .getBody()
                        .getSize()
        );


        assertEquals(
                "application/pdf",
                response
                        .getBody()
                        .getContentType()
        );


        assertEquals(
                storedFile
                        .getExpiresAt(),
                response
                        .getBody()
                        .getExpiresAt()
        );


        verify(
                fileService
        )
                .getByDownloadToken(
                        "valid-token"
                );
    }


    @Test
    void shouldIndicateWhenFileIsPasswordProtected() {

        StoredFile protectedFile =
                createStoredFile("application/pdf");

        protectedFile.setPasswordHash("bcrypt-test-hash");

        when(fileService.getByDownloadToken("protected-token"))
                .thenReturn(protectedFile);

        DownloadInfoResponse protectedInfo =
                downloadController
                        .getDownloadInfo("protected-token")
                        .getBody();

        assertNotNull(protectedInfo);
        assertTrue(protectedInfo.isPasswordProtected());

        StoredFile publicFile =
                createStoredFile("application/pdf");

        when(fileService.getByDownloadToken("public-token"))
                .thenReturn(publicFile);

        DownloadInfoResponse publicInfo =
                downloadController
                        .getDownloadInfo("public-token")
                        .getBody();

        assertNotNull(publicInfo);
        assertFalse(publicInfo.isPasswordProtected());
    }


    @Test
    void shouldDownloadFileWithPassword()
            throws Exception {

        StoredFile storedFile =
                createStoredFile(
                        "application/pdf"
                );


        Path filePath =
                createPhysicalFile();


        when(
                fileService
                        .getByDownloadToken(
                                "valid-token",
                                "Secret123!"
                        )
        )
                .thenReturn(
                        storedFile
                );


        when(
                fileService
                        .getFilePath(
                                storedFile
                        )
        )
                .thenReturn(
                        filePath
                );


        ResponseEntity<Resource>
                response =
                downloadController
                        .downloadFile(
                                "valid-token",
                                "Secret123!"
                        );


        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );


        assertNotNull(
                response.getBody()
        );


        assertTrue(
                response
                        .getBody()
                        .exists()
        );


        assertEquals(
                MediaType.APPLICATION_PDF,
                response
                        .getHeaders()
                        .getContentType()
        );


        assertEquals(
                storedFile
                        .getSize(),
                response
                        .getHeaders()
                        .getContentLength()
        );


        String contentDisposition =
                response
                        .getHeaders()
                        .getFirst(
                                "Content-Disposition"
                        );


        assertNotNull(
                contentDisposition
        );


        assertTrue(
                contentDisposition
                        .contains(
                                "attachment"
                        )
        );


        verify(
                fileService
        )
                .getByDownloadToken(
                        "valid-token",
                        "Secret123!"
                );


        verify(
                fileService
        )
                .getFilePath(
                        storedFile
                );
    }


    @Test
    void shouldUseOctetStreamWhenContentTypeIsInvalid()
            throws Exception {

        StoredFile storedFile =
                createStoredFile(
                        "invalid content type"
                );


        Path filePath =
                createPhysicalFile();


        when(
                fileService
                        .getByDownloadToken(
                                "valid-token",
                                "Secret123!"
                        )
        )
                .thenReturn(
                        storedFile
                );


        when(
                fileService
                        .getFilePath(
                                storedFile
                        )
        )
                .thenReturn(
                        filePath
                );


        ResponseEntity<Resource>
                response =
                downloadController
                        .downloadFile(
                                "valid-token",
                                "Secret123!"
                        );


        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );


        assertEquals(
                MediaType.APPLICATION_OCTET_STREAM,
                response
                        .getHeaders()
                        .getContentType()
        );


        assertNotNull(
                response.getBody()
        );


        assertTrue(
                response
                        .getBody()
                        .exists()
        );
    }


    @Test
    void shouldReturnForbiddenForWrongPassword() {

        when(
                fileService
                        .getByDownloadToken(
                                "valid-token",
                                "WrongPassword"
                        )
        )
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Mot de passe incorrect"
                        )
                );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,

                        () ->
                                downloadController
                                        .downloadFile(
                                                "valid-token",
                                                "WrongPassword"
                                        )
                );


        assertEquals(
                403,
                exception
                        .getStatusCode()
                        .value()
        );


        verify(
                fileService
        )
                .getByDownloadToken(
                        "valid-token",
                        "WrongPassword"
                );
    }


    private StoredFile createStoredFile(
            String contentType
    ) {

        User owner =
                mock(
                        User.class
                );


        return new StoredFile(
                "document.pdf",
                "stored-document.pdf",
                contentType,
                12L,
                "valid-token",
                LocalDateTime
                        .now()
                        .minusMinutes(
                                1
                        ),
                LocalDateTime
                        .now()
                        .plusDays(
                                1
                        ),
                owner
        );
    }


    private Path createPhysicalFile()
            throws Exception {

        Path filePath =
                tempDirectory.resolve(
                        "stored-file.pdf"
                );


        Files.writeString(
                filePath,
                "%PDF-1.7\nTest",
                StandardCharsets.UTF_8
        );


        return filePath;
    }
}
