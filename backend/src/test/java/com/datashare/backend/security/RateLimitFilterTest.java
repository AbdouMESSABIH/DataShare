package com.datashare.backend.security;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import static org.mockito.Mockito.mock;


class RateLimitFilterTest {

    private RateLimitFilter filter;


    @BeforeEach
    void setUp() {

        filter =
                new RateLimitFilter();
    }


    @Test
    void loginShouldBeRateLimitedAfterTenRequests()
            throws Exception {

        for (
                int i = 0;
                i < 10;
                i++
        ) {

            MockHttpServletResponse response =
                    executePost(
                            "/api/auth/login"
                    );


            assertNotEquals(
                    429,
                    response.getStatus()
            );
        }


        MockHttpServletResponse response =
                executePost(
                        "/api/auth/login"
                );


        assertEquals(
                429,
                response.getStatus()
        );


        assertEquals(
                "60",
                response.getHeader(
                        "Retry-After"
                )
        );
    }


    @Test
    void uploadShouldBeRateLimitedAfterTwentyRequests()
            throws Exception {

        for (
                int i = 0;
                i < 20;
                i++
        ) {

            MockHttpServletResponse response =
                    executePost(
                            "/api/files/upload"
                    );


            assertNotEquals(
                    429,
                    response.getStatus()
            );
        }


        MockHttpServletResponse response =
                executePost(
                        "/api/files/upload"
                );


        assertEquals(
                429,
                response.getStatus()
        );
    }


    private MockHttpServletResponse executePost(
            String path
    )
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        path
                );


        request.setRemoteAddr(
                "127.0.0.1"
        );


        MockHttpServletResponse response =
                new MockHttpServletResponse();


        FilterChain chain =
                mock(
                        FilterChain.class
                );


        filter.doFilter(
                request,
                response,
                chain
        );


        return response;
    }
}