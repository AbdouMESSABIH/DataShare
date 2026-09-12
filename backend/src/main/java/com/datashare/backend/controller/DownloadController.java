package com.datashare.backend.controller;

import com.datashare.backend.dto.DownloadInfoResponse;
import com.datashare.backend.entity.StoredFile;
import com.datashare.backend.service.FileService;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;


@RestController
@RequestMapping("/api/download")
public class DownloadController {

    private final FileService fileService;


    public DownloadController(FileService fileService) {

        this.fileService = fileService;
    }


    @GetMapping("/{token}")
    public ResponseEntity<DownloadInfoResponse> getFileInfo(
            @PathVariable String token) {

        StoredFile storedFile =
                fileService.getByDownloadToken(token);


        DownloadInfoResponse response =
                new DownloadInfoResponse(
                        storedFile.getOriginalName(),
                        storedFile.getSize(),
                        storedFile.getContentType(),
                        storedFile.getExpiresAt()
                );


        return ResponseEntity.ok(response);
    }


    @GetMapping("/{token}/file")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String token) {

        StoredFile storedFile =
                fileService.getByDownloadToken(token);

        Path filePath =
                fileService.getFilePath(storedFile);

        Resource resource =
                new FileSystemResource(filePath);


        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        if (storedFile.getContentType() != null) {

            mediaType =
                    MediaType.parseMediaType(
                            storedFile.getContentType()
                    );
        }


        ContentDisposition contentDisposition =
                ContentDisposition
                        .attachment()
                        .filename(
                                storedFile.getOriginalName(),
                                StandardCharsets.UTF_8
                        )
                        .build();


        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(storedFile.getSize())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .body(resource);
    }
}