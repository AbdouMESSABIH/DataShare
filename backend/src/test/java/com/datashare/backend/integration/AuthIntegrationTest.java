package com.datashare.backend.integration;

import com.datashare.backend.entity.User;
import com.datashare.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;


    @Test
    void registerShouldCreateUser() throws Exception {

        String email =
                uniqueEmail("register");

        String rawPassword =
                "Password123";

        String requestBody =
                registerBody(
                        email,
                        rawPassword
                );


        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(
                status().isCreated()
        );


        User savedUser =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();


        assertNotNull(
                savedUser.getId()
        );

        assertFalse(
                rawPassword.equals(
                        savedUser.getPasswordHash()
                )
        );
    }


    @Test
    void registerShouldRejectDuplicateEmail() throws Exception {

        String email =
                uniqueEmail("duplicate");

        String requestBody =
                registerBody(
                        email,
                        "Password123"
                );


        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(
                status().isCreated()
        );


        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(
                status().isConflict()
        );
    }


    @Test
    void loginShouldSucceedWithValidCredentials() throws Exception {

        String email =
                uniqueEmail("login");

        String password =
                "Password123";


        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                registerBody(
                                        email,
                                        password
                                )
                        )
        )
        .andExpect(
                status().isCreated()
        );


        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                loginBody(
                                        email,
                                        password
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );
    }


    @Test
    void loginShouldRejectInvalidPassword() throws Exception {

        String email =
                uniqueEmail("wrong-password");


        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                registerBody(
                                        email,
                                        "Password123"
                                )
                        )
        )
        .andExpect(
                status().isCreated()
        );


        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                loginBody(
                                        email,
                                        "WrongPassword123"
                                )
                        )
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    private String uniqueEmail(String prefix) {

        return prefix
                + "-"
                + UUID.randomUUID()
                + "@test.com";
    }


    private String registerBody(
            String email,
            String password
    ) {

        return """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                email,
                password
        );
    }


    private String loginBody(
            String email,
            String password
    ) {

        return """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                email,
                password
        );
    }
}