package com.datashare.backend.security;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitCorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rateLimitShouldReturnCorsHeadersOn429() throws Exception {

        String token = UUID.randomUUID().toString();

        String path = "/api/download/" + token + "/file";

        String origin = "http://localhost:4200";

        // Le token aléatoire n'existe pas :
        // les trente premières requêtes atteignent le contrôleur (404).
        for (int i = 0; i < 30; i++) {

            mockMvc.perform(
                    get(path).header("Origin", origin)
            ).andExpect(
                    status().isNotFound()
            );
        }

        // La 31e requête est bloquée avant le contrôleur.
        // Angular doit pouvoir lire le statut HTTP 429.
        mockMvc.perform(
                get(path).header("Origin", origin)
        )
        .andExpect(status().isTooManyRequests())
        .andExpect(
                header().string(
                        "Access-Control-Allow-Origin",
                        origin
                )
        )
        .andExpect(
                header().string("Retry-After", "60")
        );
    }
}
