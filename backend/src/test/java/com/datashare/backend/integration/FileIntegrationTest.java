package com.datashare.backend.integration;

import com.datashare.backend.entity.StoredFile;
import com.datashare.backend.entity.User;
import com.datashare.backend.repository.StoredFileRepository;
import com.datashare.backend.repository.UserRepository;
import com.datashare.backend.security.JwtService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoredFileRepository storedFileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;


    private final List<Path> physicalFilesToClean =
            new ArrayList<>();


    @AfterEach
    void cleanPhysicalFiles() throws Exception {

        for (Path path : physicalFilesToClean) {

            Files.deleteIfExists(path);
        }

        physicalFilesToClean.clear();
    }


    @Test
    void historyShouldBeForbiddenWithoutJwt() throws Exception {

        mockMvc.perform(
                get("/api/files")
        )
        .andExpect(
                status().isForbidden()
        );
    }


    @Test
    void authenticatedUserShouldUploadFile() throws Exception {

        String email =
                uniqueEmail("upload");

        String token =
                createUserAndToken(email);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "integration.txt",
                        "text/plain",
                        "integration upload"
                                .getBytes(StandardCharsets.UTF_8)
                );


        mockMvc.perform(
                multipart("/api/files/upload")
                        .file(file)
                        .param(
                                "expirationDays",
                                "7"
                        )
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isCreated()
        );


        StoredFile storedFile =
                getUploadedFile(email);

        trackPhysicalFile(storedFile);


        assertTrue(
                Files.exists(
                        getPhysicalPath(storedFile)
                )
        );
    }


    @Test
    void validTokenShouldDownloadFile() throws Exception {

        String email =
                uniqueEmail("download");

        String jwt =
                createUserAndToken(email);

        String fileContent =
                "download integration test";

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "download.txt",
                        "text/plain",
                        fileContent.getBytes(
                                StandardCharsets.UTF_8
                        )
                );


        mockMvc.perform(
                multipart("/api/files/upload")
                        .file(file)
                        .param(
                                "expirationDays",
                                "7"
                        )
                        .header(
                                "Authorization",
                                "Bearer " + jwt
                        )
        )
        .andExpect(
                status().isCreated()
        );


        StoredFile storedFile =
                getUploadedFile(email);

        trackPhysicalFile(storedFile);


        mockMvc.perform(
                get(
                        "/api/download/{token}/file",
                        storedFile.getDownloadToken()
                )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                content().bytes(
                        fileContent.getBytes(
                                StandardCharsets.UTF_8
                        )
                )
        );
    }


    @Test
    void invalidDownloadTokenShouldReturnNotFound()
            throws Exception {

        mockMvc.perform(
                get(
                        "/api/download/{token}/file",
                        "invalid-integration-token"
                )
        )
        .andExpect(
                status().isNotFound()
        );
    }


    @Test
    void ownerShouldDeleteOwnFile() throws Exception {

        String email =
                uniqueEmail("delete-owner");

        String jwt =
                createUserAndToken(email);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "delete.txt",
                        "text/plain",
                        "delete me"
                                .getBytes(StandardCharsets.UTF_8)
                );


        mockMvc.perform(
                multipart("/api/files/upload")
                        .file(file)
                        .param(
                                "expirationDays",
                                "7"
                        )
                        .header(
                                "Authorization",
                                "Bearer " + jwt
                        )
        )
        .andExpect(
                status().isCreated()
        );


        StoredFile storedFile =
                getUploadedFile(email);

        Path physicalPath =
                getPhysicalPath(storedFile);

        trackPhysicalFile(storedFile);


        assertTrue(
                Files.exists(physicalPath)
        );


        mockMvc.perform(
                delete(
                        "/api/files/{id}",
                        storedFile.getId()
                )
                .header(
                        "Authorization",
                        "Bearer " + jwt
                )
        )
        .andExpect(
                status().isNoContent()
        );


        assertFalse(
                storedFileRepository
                        .findById(
                                storedFile.getId()
                        )
                        .isPresent()
        );

        assertFalse(
                Files.exists(physicalPath)
        );
    }


    @Test
    void userShouldNotDeleteAnotherUsersFile()
            throws Exception {

        String ownerEmail =
                uniqueEmail("owner");

        String otherEmail =
                uniqueEmail("other");


        String ownerJwt =
                createUserAndToken(ownerEmail);

        String otherJwt =
                createUserAndToken(otherEmail);


        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "private.txt",
                        "text/plain",
                        "private content"
                                .getBytes(StandardCharsets.UTF_8)
                );


        mockMvc.perform(
                multipart("/api/files/upload")
                        .file(file)
                        .param(
                                "expirationDays",
                                "7"
                        )
                        .header(
                                "Authorization",
                                "Bearer " + ownerJwt
                        )
        )
        .andExpect(
                status().isCreated()
        );


        StoredFile storedFile =
                getUploadedFile(ownerEmail);

        Path physicalPath =
                getPhysicalPath(storedFile);

        trackPhysicalFile(storedFile);


        mockMvc.perform(
                delete(
                        "/api/files/{id}",
                        storedFile.getId()
                )
                .header(
                        "Authorization",
                        "Bearer " + otherJwt
                )
        )
        .andExpect(
                status().isNotFound()
        );


        assertTrue(
                storedFileRepository
                        .findById(
                                storedFile.getId()
                        )
                        .isPresent()
        );

        assertTrue(
                Files.exists(physicalPath)
        );
    }


    private String createUserAndToken(String email) {

        User user =
                new User(
                        email,
                        passwordEncoder.encode(
                                "Password123"
                        )
                );

        userRepository.save(user);

        return jwtService.generateToken(email);
    }


    private StoredFile getUploadedFile(String email) {

        return storedFileRepository
                .findByOwnerEmailOrderByCreatedAtDesc(email)
                .stream()
                .findFirst()
                .orElseThrow();
    }


    private Path getPhysicalPath(
            StoredFile storedFile
    ) {

        return Paths.get("uploads")
                .resolve(
                        storedFile.getStorageName()
                );
    }


    private void trackPhysicalFile(
            StoredFile storedFile
    ) {

        physicalFilesToClean.add(
                getPhysicalPath(storedFile)
        );
    }


    private String uniqueEmail(String prefix) {

        return prefix
                + "-"
                + UUID.randomUUID()
                + "@test.com";
    }
}