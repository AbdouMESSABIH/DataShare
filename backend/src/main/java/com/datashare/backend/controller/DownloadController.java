package com.datashare.backend.controller;

import com.datashare.backend.dto.DownloadInfoResponse;
import com.datashare.backend.entity.StoredFile;
import com.datashare.backend.service.FileService;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;


@RestController
@RequestMapping("/api/download")
public class DownloadController {

    private final FileService fileService;


    public DownloadController(
            FileService fileService
    ) {

        this.fileService =
                fileService;
    }


    @GetMapping("/{token}")
    public ResponseEntity<DownloadInfoResponse>
    getDownloadInfo(
            @PathVariable String token
    ) {

        StoredFile storedFile =
                fileService
                        .getByDownloadToken(
                                token
                        );


        DownloadInfoResponse response =
                new DownloadInfoResponse(
                        storedFile
                                .getOriginalName(),

                        storedFile
                                .getSize(),

                        storedFile
                                .getContentType(),

                        storedFile
                                .getExpiresAt(),

                        storedFile.getPasswordHash() != null
                                && !storedFile.getPasswordHash().isBlank()
                );


        return ResponseEntity.ok(
                response
        );
    }


    @GetMapping("/{token}/file")
    public ResponseEntity<Resource>
    downloadFile(
            @PathVariable String token,

            @RequestHeader(
                    value = "X-Download-Password",
                    required = false
            )
            String password
    ) {

        StoredFile storedFile =
                fileService
                        .getByDownloadToken(
                                token,
                                password
                        );


        Path filePath =
                fileService
                        .getFilePath(
                                storedFile
                        );


        Resource resource =
                new FileSystemResource(
                        filePath
                );


        ContentDisposition disposition =
                ContentDisposition
                        .attachment()
                        .filename(
                                storedFile
                                        .getOriginalName(),

                                StandardCharsets.UTF_8
                        )
                        .build();


        MediaType mediaType =
                resolveMediaType(
                        storedFile
                                .getContentType()
                );


        return ResponseEntity
                .ok()

                .contentType(
                        mediaType
                )

                .contentLength(
                        storedFile
                                .getSize()
                )

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )

                .body(
                        resource
                );
    }


    private MediaType resolveMediaType(
            String contentType
    ) {

        if (
                contentType == null
                || contentType.isBlank()
        ) {

            return MediaType
                    .APPLICATION_OCTET_STREAM;
        }


        try {

            return MediaType
                    .parseMediaType(
                            contentType
                    );

        } catch (
                InvalidMediaTypeException exception
        ) {

            return MediaType
                    .APPLICATION_OCTET_STREAM;
        }
    }
}
