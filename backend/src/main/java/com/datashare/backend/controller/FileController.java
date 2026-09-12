package com.datashare.backend.controller;

import com.datashare.backend.dto.UploadResponse;
import com.datashare.backend.service.FileService;
import com.datashare.backend.dto.FileHistoryResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expirationDays", required = false)
            Integer expirationDays,
            Authentication authentication
    ) {

        String email = authentication.getName();

        UploadResponse response = fileService.upload(
                file,
                email,
                expirationDays
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<FileHistoryResponse>> getHistory(
            Authentication authentication
    ) {

        String email = authentication.getName();

        List<FileHistoryResponse> history =
                fileService.getHistory(email);

        return ResponseEntity.ok(history);
    }
}