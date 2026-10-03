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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.test.util.ReflectionTestUtils;

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
                        userRepository,
                        new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12)
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
                                        7,
                                null
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
                        "archive.exe",
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
                                        7,
                                null
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
                                        7,
                                null
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
                                        7,
                                null
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
                                        7,
                                null
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
                                        8,
                                null
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
                                        7,
                                null
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



    // Teste le validateur avant toute ecriture sur le disque.
    private void assertAcceptedFormat(
            String filename,
            String extension,
            String expectedMime,
            byte[] bytes
    ) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                expectedMime,
                bytes
        );

        String result = ReflectionTestUtils.invokeMethod(
                fileService,
                "validateRealContentType",
                file,
                extension
        );

        assertEquals(expectedMime, result);
    }


    private void assertRejectedFormat(
            String filename,
            String extension,
            String declaredMime,
            byte[] bytes
    ) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                declaredMime,
                bytes
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        fileService,
                        "validateRealContentType",
                        file,
                        extension
                )
        );

        assertEquals(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                exception.getStatusCode()
        );
    }


    // Contrairement a un simple en-tete PK, ceci cree une archive ZIP.
    private byte[] createValidZip() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            zip.putNextEntry(new ZipEntry("bonjour.txt"));
            zip.write("Bonjour DataShare".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        return output.toByteArray();
    }


    @Test
    void shouldAcceptValidZipSignature() throws IOException {
        assertAcceptedFormat(
                "archive.zip",
                "zip",
                "application/zip",
                createValidZip()
        );
    }


    @Test
    void shouldAcceptValidMp4Header() {
        byte[] mp4 = {
                0, 0, 0, 24,
                'f', 't', 'y', 'p',
                'i', 's', 'o', 'm',
                0, 0, 2, 0,
                'i', 's', 'o', 'm',
                'm', 'p', '4', '2'
        };

        assertAcceptedFormat(
                "video.mp4",
                "mp4",
                "video/mp4",
                mp4
        );
    }


    @Test
    void shouldAcceptMp3WithId3Header() {
        byte[] mp3 = {
                'I', 'D', '3',
                4, 0, 0,
                0, 0, 0, 0,
                (byte) 0xFF,
                (byte) 0xFB,
                (byte) 0x90,
                0x64
        };

        assertAcceptedFormat(
                "audio.mp3",
                "mp3",
                "audio/mpeg",
                mp3
        );
    }


    @Test
    void shouldAcceptMp3WithMpegHeader() {
        byte[] mp3 = {
                (byte) 0xFF,
                (byte) 0xFB,
                (byte) 0x90,
                0x64
        };

        assertAcceptedFormat(
                "audio.mp3",
                "mp3",
                "audio/mpeg",
                mp3
        );
    }


    @Test
    void shouldRejectFakeMp3() {
        assertRejectedFormat(
                "faux.mp3",
                "mp3",
                "audio/mpeg",
                "Ceci est du texte".getBytes(StandardCharsets.UTF_8)
        );
    }


    @Test
    void shouldRejectFakeMp4() {
        assertRejectedFormat(
                "faux.mp4",
                "mp4",
                "video/mp4",
                "%PDF-1.7\\n".getBytes(StandardCharsets.UTF_8)
        );
    }


    @Test
    void shouldRejectFakeZip() {
        assertRejectedFormat(
                "faux.zip",
                "zip",
                "application/zip",
                "Ceci est du texte".getBytes(StandardCharsets.UTF_8)
        );
    }


    @Test
    void shouldRejectTruncatedZipHeader() {
        assertRejectedFormat(
                "incomplet.zip",
                "zip",
                "application/zip",
                new byte[]{0x50, 0x4B, 0x03, 0x04}
        );
    }


    @Test
    void shouldRejectZipDisguisedAsMp4() throws IOException {
        assertRejectedFormat(
                "archive.mp4",
                "mp4",
                "video/mp4",
                createValidZip()
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