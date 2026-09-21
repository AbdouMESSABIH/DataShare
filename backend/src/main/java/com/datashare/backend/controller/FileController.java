package com.datashare.backend.controller;

import com.datashare.backend.dto.FileHistoryPageResponse;
import com.datashare.backend.dto.UploadResponse;
import com.datashare.backend.service.FileService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;


    public FileController(
            FileService fileService
    ) {

        this.fileService =
                fileService;
    }


    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("file")
            MultipartFile file,

            @RequestParam(
                    value = "expirationDays",
                    required = false
            )
            Integer expirationDays,

            Authentication authentication
    ) {

        String email =
                authentication.getName();


        UploadResponse response =
                fileService.upload(
                        file,
                        email,
                        expirationDays
                );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping
    public ResponseEntity<FileHistoryPageResponse>
    getHistory(

            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size,

            Authentication authentication
    ) {

        String email =
                authentication.getName();


        FileHistoryPageResponse history =
                fileService.getHistory(
                        email,
                        page,
                        size
                );


        return ResponseEntity.ok(
                history
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email =
                authentication.getName();


        fileService.deleteFile(
                id,
                email
        );


        return ResponseEntity
                .noContent()
                .build();
    }
}