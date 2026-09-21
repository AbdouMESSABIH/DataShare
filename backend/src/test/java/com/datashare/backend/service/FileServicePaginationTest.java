package com.datashare.backend.service;

import com.datashare.backend.dto.FileHistoryPageResponse;
import com.datashare.backend.entity.StoredFile;
import com.datashare.backend.repository.StoredFileRepository;
import com.datashare.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class FileServicePaginationTest {

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
                        userRepository
                );
    }


    @Test
    void historyShouldReturnPaginatedFiles() {

        StoredFile storedFile =
                mock(
                        StoredFile.class
                );


        when(
                storedFile.getId()
        ).thenReturn(1L);

        when(
                storedFile.getOriginalName()
        ).thenReturn(
                "test.txt"
        );

        when(
                storedFile.getSize()
        ).thenReturn(
                100L
        );

        when(
                storedFile.getContentType()
        ).thenReturn(
                "text/plain"
        );

        when(
                storedFile.getDownloadToken()
        ).thenReturn(
                "token"
        );

        when(
                storedFile.getCreatedAt()
        ).thenReturn(
                LocalDateTime.now()
        );

        when(
                storedFile.getExpiresAt()
        ).thenReturn(
                LocalDateTime.now()
                        .plusDays(7)
        );


        PageRequest pageable =
                PageRequest.of(
                        0,
                        10
                );


        when(
                storedFileRepository
                        .findByOwnerEmailOrderByCreatedAtDesc(
                                eq("test@test.com"),
                                any(Pageable.class)
                        )
        ).thenReturn(
                new PageImpl<>(
                        List.of(
                                storedFile
                        ),
                        pageable,
                        1
                )
        );


        FileHistoryPageResponse result =
                fileService.getHistory(
                        "test@test.com",
                        0,
                        10
                );


        assertEquals(
                1,
                result.getContent()
                        .size()
        );


        assertEquals(
                "test.txt",
                result.getContent()
                        .getFirst()
                        .getOriginalName()
        );


        assertEquals(
                0,
                result.getPage()
        );


        assertEquals(
                10,
                result.getSize()
        );


        assertEquals(
                1,
                result.getTotalElements()
        );


        assertEquals(
                1,
                result.getTotalPages()
        );
    }


    @Test
    void historyShouldRejectNegativePage() {

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                fileService.getHistory(
                                        "test@test.com",
                                        -1,
                                        10
                                )
                );


        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );
    }


    @Test
    void historyShouldRejectPageSizeGreaterThanFifty() {

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                fileService.getHistory(
                                        "test@test.com",
                                        0,
                                        51
                                )
                );


        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );
    }
}