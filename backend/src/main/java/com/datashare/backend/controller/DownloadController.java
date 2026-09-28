package com.datashare.backend.controller;

import com.datashare.backend.entity.StoredFile;
import com.datashare.backend.service.FileService;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import java.util.LinkedHashMap;
import java.util.Map;


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
    public ResponseEntity<Map<String, Object>>
    getDownloadInfo(
            @PathVariable String token
    ) {

        StoredFile storedFile =
                fileService
                        .getByDownloadToken(
                                token
                        );


        Map<String, Object> response =
                new LinkedHashMap<>();


        response.put(
                "originalName",
                storedFile.getOriginalName()
        );

        response.put(
                "size",
                storedFile.getSize()
        );

        response.put(
                "contentType",
                storedFile.getContentType()
        );

        response.put(
                "expiresAt",
                storedFile.getExpiresAt()
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


        return ResponseEntity
                .ok()
                .contentType(
                        MediaType.parseMediaType(
                                storedFile
                                        .getContentType()
                        )
                )
                .contentLength(
                        storedFile.getSize()
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .body(
                        resource
                );
    }
}
