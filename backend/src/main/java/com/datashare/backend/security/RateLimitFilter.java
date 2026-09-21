package com.datashare.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.concurrent.ConcurrentHashMap;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter
        extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS =
            60_000L;


    private static final int LOGIN_LIMIT =
            10;


    private static final int UPLOAD_LIMIT =
            20;


    private final ConcurrentHashMap<
            String,
            WindowCounter
            > counters =
            new ConcurrentHashMap<>();


    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        if (
                !"POST".equalsIgnoreCase(
                        request.getMethod()
                )
        ) {

            return true;
        }


        String path =
                request.getRequestURI();


        return !path.equals(
                "/api/auth/login"
        )
                && !path.equals(
                        "/api/files/upload"
                );
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException,
            IOException {

        String path =
                request.getRequestURI();


        int limit =
                path.equals(
                        "/api/auth/login"
                )
                        ? LOGIN_LIMIT
                        : UPLOAD_LIMIT;


        long currentWindow =
                System.currentTimeMillis()
                        / WINDOW_MILLIS;


        String clientAddress =
                request.getRemoteAddr();


        String key =
                path
                        + "|"
                        + clientAddress;


        WindowCounter counter =
                counters.compute(
                        key,
                        (
                                ignored,
                                previous
                        ) -> {

                            if (
                                    previous == null
                                    || previous.window()
                                    != currentWindow
                            ) {

                                return new WindowCounter(
                                        currentWindow,
                                        1
                                );
                            }


                            return new WindowCounter(
                                    currentWindow,
                                    previous.count() + 1
                            );
                        }
                );


        cleanupOldCounters(
                currentWindow
        );


        if (
                counter.count()
                > limit
        ) {

            response.setStatus(
                    429
            );

            response.setContentType(
                    "application/json"
            );

            response.setCharacterEncoding(
                    "UTF-8"
            );

            response.setHeader(
                    "Retry-After",
                    "60"
            );


            response
                    .getWriter()
                    .write(
                            """
                            {"error":"Trop de requêtes. Réessayez dans une minute."}
                            """
                    );


            return;
        }


        filterChain.doFilter(
                request,
                response
        );
    }


    private void cleanupOldCounters(
            long currentWindow
    ) {

        if (
                counters.size()
                < 10_000
        ) {

            return;
        }


        counters.entrySet()
                .removeIf(
                        entry ->
                                entry
                                        .getValue()
                                        .window()
                                        < currentWindow - 1
                );
    }


    private record WindowCounter(
            long window,
            int count
    ) {
    }
}