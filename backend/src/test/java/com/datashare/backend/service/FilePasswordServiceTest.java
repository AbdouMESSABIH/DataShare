package com.datashare.backend.service;

import com.datashare.backend.entity.StoredFile;
import com.datashare.backend.entity.User;
import com.datashare.backend.repository.StoredFileRepository;
import com.datashare.backend.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

import org.springframework.mock.web.MockMultipartFile;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;


class FilePasswordServiceTest {

    private StoredFileRepository
            storedFileRepository;

    private UserRepository
            userRepository;

    private PasswordEncoder
            passwordEncoder;

    private FileService
            fileService;


    private final Path uploadDirectory =
            Paths.get("uploads");


    private final List<Path> filesToDelete =
            new ArrayList<>();


    @BeforeEach
    void setUp()
            throws Exception {

        storedFileRepository =
                mock(
                        StoredFileRepository.class
                );


        userRepository =
                mock(
                        UserRepository.class
                );


        passwordEncoder =
                new BCryptPasswordEncoder();


        fileService =
                new FileService(
                        storedFileRepository,
                        userRepository,
                        passwordEncoder
                );


        Files.createDirectories(
                uploadDirectory
        );
    }


    @AfterEach
    void cleanUp()
            throws Exception {

        for (
                Path file :
                filesToDelete
        ) {

            Files.deleteIfExists(
                    file
            );
        }


        filesToDelete.clear();
    }


    @Test
    void uploadShouldRejectPasswordShorterThanSixCharacters() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "%PDF-1.7\\nTest".getBytes(StandardCharsets.UTF_8)
        );

        for (String password : List.of("12345", "  abc  ")) {

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> fileService.upload(
                            file,
                            "test@datashare.fr",
                            1,
                            password
                    )
            );

            assertEquals(400, exception.getStatusCode().value());
        }

        verifyNoInteractions(userRepository, storedFileRepository);
    }


    /*
     * TEST 1
     *
     * Vérifie que le mot de passe du fichier
     * n'est jamais enregistré en clair.
     *
     * Il doit être transformé en hash BCrypt.
     */
    @Test
    void uploadShouldHashPassword()
            throws Exception {

        User owner =
                mock(
                        User.class
                );


        when(
                owner.getEmail()
        )
                .thenReturn(
                        "test@datashare.fr"
                );


        when(
                userRepository.findByEmail(
                        "test@datashare.fr"
                )
        )
                .thenReturn(
                        Optional.of(
                                owner
                        )
                );


        when(
                storedFileRepository.save(
                        any(
                                StoredFile.class
                        )
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(
                                        0
                                )
                );


        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "document.pdf",
                        "application/pdf",
                        "%PDF-1.7\nContenu de test"
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );


        fileService.upload(
                file,
                "test@datashare.fr",
                1,
                "Secret123!"
        );


        ArgumentCaptor<StoredFile>
                captor =
                ArgumentCaptor.forClass(
                        StoredFile.class
                );


        verify(
                storedFileRepository
        )
                .save(
                        captor.capture()
                );


        StoredFile savedFile =
                captor.getValue();


        filesToDelete.add(
                uploadDirectory.resolve(
                        savedFile
                                .getStorageName()
                )
        );


        assertNotNull(
                savedFile.getPasswordHash()
        );


        assertNotEquals(
                "Secret123!",
                savedFile.getPasswordHash()
        );


        assertTrue(
                passwordEncoder.matches(
                        "Secret123!",
                        savedFile.getPasswordHash()
                )
        );
    }


    /*
     * TEST 2
     *
     * Bon mot de passe :
     * le téléchargement doit être autorisé.
     */
    @Test
    void downloadShouldSucceedWithCorrectPassword()
            throws Exception {

        StoredFile storedFile =
                createStoredFile(
                        "Secret123!"
                );


        when(
                storedFileRepository
                        .findByDownloadToken(
                                storedFile
                                        .getDownloadToken()
                        )
        )
                .thenReturn(
                        Optional.of(
                                storedFile
                        )
                );


        StoredFile result =
                fileService
                        .getByDownloadToken(
                                storedFile
                                        .getDownloadToken(),
                                "Secret123!"
                        );


        assertSame(
                storedFile,
                result
        );
    }


    /*
     * TEST 3
     *
     * Mauvais mot de passe :
     * le backend doit refuser avec HTTP 403.
     */
    @Test
    void downloadShouldReturn403WithWrongPassword()
            throws Exception {

        StoredFile storedFile =
                createStoredFile(
                        "Secret123!"
                );


        when(
                storedFileRepository
                        .findByDownloadToken(
                                storedFile
                                        .getDownloadToken()
                        )
        )
                .thenReturn(
                        Optional.of(
                                storedFile
                        )
                );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                fileService
                                        .getByDownloadToken(
                                                storedFile
                                                        .getDownloadToken(),
                                                "MauvaisMotDePasse"
                                        )
                );


        assertEquals(
                403,
                exception
                        .getStatusCode()
                        .value()
        );
    }


    /*
     * TEST 4
     *
     * Fichier protégé mais aucun mot de passe :
     * le backend doit refuser avec HTTP 403.
     */
    @Test
    void downloadShouldReturn403WhenPasswordIsMissing()
            throws Exception {

        StoredFile storedFile =
                createStoredFile(
                        "Secret123!"
                );


        when(
                storedFileRepository
                        .findByDownloadToken(
                                storedFile
                                        .getDownloadToken()
                        )
        )
                .thenReturn(
                        Optional.of(
                                storedFile
                        )
                );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                fileService
                                        .getByDownloadToken(
                                                storedFile
                                                        .getDownloadToken(),
                                                null
                                        )
                );


        assertEquals(
                403,
                exception
                        .getStatusCode()
                        .value()
        );
    }


    /*
     * TEST 5
     *
     * Un ancien fichier sans mot de passe
     * doit rester téléchargeable.
     *
     * Cela permet de garder la compatibilité
     * avec les fichiers créés avant
     * l'ajout de cette fonctionnalité.
     */
    @Test
    void unprotectedFileShouldStillBeDownloadable()
            throws Exception {

        StoredFile storedFile =
                createStoredFile(
                        null
                );


        when(
                storedFileRepository
                        .findByDownloadToken(
                                storedFile
                                        .getDownloadToken()
                        )
        )
                .thenReturn(
                        Optional.of(
                                storedFile
                        )
                );


        StoredFile result =
                fileService
                        .getByDownloadToken(
                                storedFile
                                        .getDownloadToken(),
                                null
                        );


        assertSame(
                storedFile,
                result
        );
    }


    /*
     * Méthode utilitaire :
     *
     * crée un vrai petit fichier temporaire
     * dans le dossier uploads afin que
     * FileService puisse vérifier son existence.
     */
    private StoredFile createStoredFile(
            String password
    )
            throws Exception {

        String storageName =
                UUID.randomUUID()
                        + ".pdf";


        String downloadToken =
                UUID.randomUUID()
                        .toString();


        Path filePath =
                uploadDirectory.resolve(
                        storageName
                );


        Files.writeString(
                filePath,
                "%PDF-1.7\nTest",
                StandardCharsets.UTF_8
        );


        filesToDelete.add(
                filePath
        );


        User owner =
                mock(
                        User.class
                );


        StoredFile storedFile =
                new StoredFile(
                        "document.pdf",
                        storageName,
                        "application/pdf",
                        Files.size(
                                filePath
                        ),
                        downloadToken,
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


        if (password != null) {

            storedFile.setPasswordHash(
                    passwordEncoder.encode(
                            password
                    )
            );
        }


        return storedFile;
    }
}
