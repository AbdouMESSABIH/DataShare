package com.datashare.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000L;

    private static final int LOGIN_LIMIT = 10;
    private static final int UPLOAD_LIMIT = 20;

    private static final int DOWNLOAD_IP_LIMIT = 60;
    private static final int DOWNLOAD_TOKEN_LIMIT = 30;

    private static final String DOWNLOAD_PREFIX = "/api/download/";
    private static final String DOWNLOAD_SUFFIX = "/file";

    private final ConcurrentHashMap<String, WindowCounter> counters =
            new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("POST".equalsIgnoreCase(method)) {
            return !path.equals("/api/auth/login")
                    && !path.equals("/api/files/upload");
        }

        if ("GET".equalsIgnoreCase(method)) {
            return !isDownloadFilePath(path);
        }

        return true;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String clientAddress = request.getRemoteAddr();

        long currentWindow =
                System.currentTimeMillis() / WINDOW_MILLIS;

        if (path.equals("/api/auth/login")) {

            if (increment("login|" + clientAddress, currentWindow)
                    > LOGIN_LIMIT) {
                reject(response);
                return;
            }

        } else if (path.equals("/api/files/upload")) {

            if (increment("upload|" + clientAddress, currentWindow)
                    > UPLOAD_LIMIT) {
                reject(response);
                return;
            }

        } else if (isDownloadFilePath(path)) {

            // Première protection : nombre total par adresse IP.
            if (increment(
                    "download-ip|" + clientAddress,
                    currentWindow
            ) > DOWNLOAD_IP_LIMIT) {

                reject(response);
                return;
            }

            // Deuxième protection : limite globale du token.
            // Le token n'est jamais envoyé dans les logs.
            String token = path.substring(
                    DOWNLOAD_PREFIX.length(),
                    path.length() - DOWNLOAD_SUFFIX.length()
            );

            if (increment(
                    "download-token|" + token,
                    currentWindow
            ) > DOWNLOAD_TOKEN_LIMIT) {

                reject(response);
                return;
            }
        }

        cleanupOldCounters(currentWindow);

        filterChain.doFilter(request, response);
    }

    private boolean isDownloadFilePath(String path) {

        if (!path.startsWith(DOWNLOAD_PREFIX)
                || !path.endsWith(DOWNLOAD_SUFFIX)) {
            return false;
        }

        if (path.length() <= DOWNLOAD_PREFIX.length()
                + DOWNLOAD_SUFFIX.length()) {
            return false;
        }

        String token = path.substring(
                DOWNLOAD_PREFIX.length(),
                path.length() - DOWNLOAD_SUFFIX.length()
        );

        return !token.isBlank() && !token.contains("/");
    }

    private int increment(String key, long currentWindow) {

        WindowCounter result = counters.compute(
                key,
                (ignored, previous) -> {

                    if (previous == null
                            || previous.window() != currentWindow) {

                        return new WindowCounter(currentWindow, 1);
                    }

                    return new WindowCounter(
                            currentWindow,
                            previous.count() + 1
                    );
                }
        );

        return result.count();
    }

    private void reject(HttpServletResponse response)
            throws IOException {

        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", "60");
        response.setHeader("Cache-Control", "no-store");

        response.getWriter().write(
                "{\"error\":\"Trop de requêtes. Réessayez dans une minute.\"}"
        );
    }

    private void cleanupOldCounters(long currentWindow) {

        if (counters.size() < 10_000) {
            return;
        }

        counters.entrySet().removeIf(
                entry -> entry.getValue().window() < currentWindow - 1
        );
    }

    private record WindowCounter(long window, int count) {
    }
}
