package com.datashare.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
    }

    @Test
    void loginShouldBeRateLimitedAfterTenRequests()
            throws Exception {

        for (int i = 0; i < 10; i++) {
            assertAllowed(execute(
                    "POST",
                    "/api/auth/login",
                    "127.0.0.1"
            ));
        }

        assertLimited(execute(
                "POST",
                "/api/auth/login",
                "127.0.0.1"
        ));
    }

    @Test
    void uploadShouldBeRateLimitedAfterTwentyRequests()
            throws Exception {

        for (int i = 0; i < 20; i++) {
            assertAllowed(execute(
                    "POST",
                    "/api/files/upload",
                    "127.0.0.1"
            ));
        }

        assertLimited(execute(
                "POST",
                "/api/files/upload",
                "127.0.0.1"
        ));
    }

    @Test
    void downloadShouldBeLimitedAfterThirtyRequestsPerToken()
            throws Exception {

        String path = "/api/download/token-test/file";

        for (int i = 0; i < 30; i++) {
            assertAllowed(execute(
                    "GET",
                    path,
                    "127.0.0.1"
            ));
        }

        assertLimited(execute(
                "GET",
                path,
                "127.0.0.1"
        ));
    }

    @Test
    void downloadShouldBeLimitedPerIpAcrossDifferentTokens()
            throws Exception {

        String ip = "192.0.2.10";

        for (int i = 0; i < 60; i++) {
            assertAllowed(execute(
                    "GET",
                    "/api/download/token-" + i + "/file",
                    ip
            ));
        }

        assertLimited(execute(
                "GET",
                "/api/download/another-token/file",
                ip
        ));
    }

    @Test
    void downloadTokenLimitShouldApplyAcrossDifferentIpAddresses()
            throws Exception {

        String path = "/api/download/shared-token/file";

        for (int i = 0; i < 30; i++) {
            assertAllowed(execute(
                    "GET",
                    path,
                    "192.0.2." + (i + 1)
            ));
        }

        assertLimited(execute(
                "GET",
                path,
                "198.51.100.1"
        ));
    }

    @Test
    void downloadMetadataShouldRemainAccessible()
            throws Exception {

        for (int i = 0; i < 65; i++) {
            assertAllowed(execute(
                    "GET",
                    "/api/download/metadata-token",
                    "127.0.0.1"
            ));
        }
    }

    private MockHttpServletResponse execute(
            String method,
            String path,
            String ip
    ) throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(method, path);

        request.setRemoteAddr(ip);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        // Ce marqueur permet de vérifier qu'une requête refusée
        // n'arrive jamais au contrôleur ni à la vérification BCrypt.
        FilterChain chain = (servletRequest, servletResponse) ->
                ((HttpServletResponse) servletResponse)
                        .setHeader("X-Filter-Continued", "true");

        filter.doFilter(request, response, chain);

        return response;
    }

    private void assertAllowed(MockHttpServletResponse response) {

        assertEquals(200, response.getStatus());

        assertEquals(
                "true",
                response.getHeader("X-Filter-Continued")
        );
    }

    private void assertLimited(MockHttpServletResponse response) {

        assertEquals(429, response.getStatus());

        assertEquals(
                "60",
                response.getHeader("Retry-After")
        );

        assertNull(
                response.getHeader("X-Filter-Continued")
        );
    }
}
