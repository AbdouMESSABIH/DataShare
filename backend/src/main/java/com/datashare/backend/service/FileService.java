package com.datashare.backend.service;

import com.datashare.backend.dto.FileHistoryPageResponse;
import com.datashare.backend.dto.FileHistoryResponse;
import com.datashare.backend.dto.UploadResponse;
import com.datashare.backend.entity.StoredFile;
import com.datashare.backend.entity.User;
import com.datashare.backend.repository.StoredFileRepository;
import com.datashare.backend.repository.UserRepository;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


@Service
public class FileService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    FileService.class
            );


    private static final long MAX_FILE_SIZE =
            1024L * 1024 * 1024;


    private static final int MAX_EXPIRATION_DAYS =
            7;


    private static final int CONTENT_SAMPLE_SIZE =
            8192;


    private static final int MAX_PAGE_SIZE =
            50;


    private static final Map<String, String>
            ALLOWED_FILE_TYPES =
            Map.of(
                    "txt", "text/plain",
                    "pdf", "application/pdf",
                    "png", "image/png",
                    "jpg", "image/jpeg",
                    "jpeg", "image/jpeg"
            );


    private final StoredFileRepository
            storedFileRepository;


    private final UserRepository
            userRepository;


    private final Path uploadDirectory =
            Paths.get("uploads");


    public FileService(
            StoredFileRepository storedFileRepository,
            UserRepository userRepository
    ) {

        this.storedFileRepository =
                storedFileRepository;

        this.userRepository =
                userRepository;
    }


    @PostConstruct
    void initializeUploadDirectory() {

        try {

            Files.createDirectories(
                    uploadDirectory
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Impossible de créer le dossier uploads",
                    e
            );
        }
    }


    public UploadResponse upload(
            MultipartFile file,
            String email,
            Integer expirationDays
    ) {

        if (file.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le fichier est vide"
            );
        }


        if (
                file.getSize()
                > MAX_FILE_SIZE
        ) {

            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Le fichier dépasse la taille maximale de 1 Go"
            );
        }


        String originalName =
                StringUtils.cleanPath(
                        file.getOriginalFilename() == null
                                ? "file"
                                : file.getOriginalFilename()
                );


        String extension =
                getAllowedExtension(
                        originalName
                );


        String detectedContentType =
                validateRealContentType(
                        file,
                        extension
                );


        int days =
                expirationDays == null
                        ? MAX_EXPIRATION_DAYS
                        : expirationDays;


        if (
                days < 1
                || days > MAX_EXPIRATION_DAYS
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La durée d'expiration doit être comprise entre 1 et 7 jours"
            );
        }


        User owner =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Utilisateur introuvable"
                                        )
                        );


        String storageName =
                UUID.randomUUID()
                        + "."
                        + extension;


        String downloadToken =
                UUID.randomUUID()
                        .toString();


        LocalDateTime createdAt =
                LocalDateTime.now();


        LocalDateTime expiresAt =
                createdAt.plusDays(
                        days
                );


        Path targetPath =
                uploadDirectory.resolve(
                        storageName
                );


        try {

            file.transferTo(
                    targetPath
            );

        } catch (
                IOException
                | IllegalStateException e
        ) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Impossible d'enregistrer le fichier"
            );
        }


        StoredFile storedFile =
                new StoredFile(
                        originalName,
                        storageName,
                        detectedContentType,
                        file.getSize(),
                        downloadToken,
                        createdAt,
                        expiresAt,
                        owner
                );


        StoredFile savedFile =
                storedFileRepository.save(
                        storedFile
                );


        log.atInfo()
                .addKeyValue(
                        "event",
                        "file_upload"
                )
                .addKeyValue(
                        "fileId",
                        savedFile.getId()
                )
                .addKeyValue(
                        "size",
                        savedFile.getSize()
                )
                .addKeyValue(
                        "owner",
                        savedFile
                                .getOwner()
                                .getEmail()
                )
                .log(
                        "File uploaded successfully"
                );


        return new UploadResponse(
                savedFile.getId(),
                savedFile.getOriginalName(),
                savedFile.getSize(),
                savedFile.getDownloadToken(),
                savedFile.getExpiresAt()
        );
    }


    private String getAllowedExtension(
            String originalName
    ) {

        String extension =
                StringUtils
                        .getFilenameExtension(
                                originalName
                        );


        if (extension == null) {

            throwUnsupportedFileType();
        }


        String normalizedExtension =
                extension.toLowerCase(
                        Locale.ROOT
                );


        if (
                !ALLOWED_FILE_TYPES
                        .containsKey(
                                normalizedExtension
                        )
        ) {

            throwUnsupportedFileType();
        }


        return normalizedExtension;
    }


    private String validateRealContentType(
            MultipartFile file,
            String extension
    ) {

        String expectedContentType =
                ALLOWED_FILE_TYPES.get(
                        extension
                );


        String detectedContentType =
                detectContentType(
                        file
                );


        if (
                !expectedContentType.equals(
                        detectedContentType
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "L'extension du fichier ne correspond pas à son contenu"
            );
        }


        return detectedContentType;
    }


    private String detectContentType(
            MultipartFile file
    ) {

        byte[] sample =
                new byte[
                        CONTENT_SAMPLE_SIZE
                ];


        int bytesRead;


        try (
                InputStream inputStream =
                        file.getInputStream()
        ) {

            bytesRead =
                    inputStream.read(
                            sample
                    );

        } catch (IOException e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Impossible de lire le contenu du fichier"
            );
        }


        if (bytesRead <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le fichier est vide"
            );
        }


        if (
                isPdf(
                        sample,
                        bytesRead
                )
        ) {

            return "application/pdf";
        }


        if (
                isPng(
                        sample,
                        bytesRead
                )
        ) {

            return "image/png";
        }


        if (
                isJpeg(
                        sample,
                        bytesRead
                )
        ) {

            return "image/jpeg";
        }


        if (
                isUtf8Text(
                        sample,
                        bytesRead
                )
        ) {

            return "text/plain";
        }


        return "application/octet-stream";
    }


    private boolean isPdf(
            byte[] data,
            int length
    ) {

        return length >= 5
                && data[0] == '%'
                && data[1] == 'P'
                && data[2] == 'D'
                && data[3] == 'F'
                && data[4] == '-';
    }


    private boolean isPng(
            byte[] data,
            int length
    ) {

        int[] signature = {
                0x89,
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A
        };


        if (
                length
                < signature.length
        ) {

            return false;
        }


        for (
                int i = 0;
                i < signature.length;
                i++
        ) {

            if (
                    Byte.toUnsignedInt(
                            data[i]
                    )
                    != signature[i]
            ) {

                return false;
            }
        }


        return true;
    }


    private boolean isJpeg(
            byte[] data,
            int length
    ) {

        return length >= 3
                && Byte.toUnsignedInt(
                        data[0]
                ) == 0xFF
                && Byte.toUnsignedInt(
                        data[1]
                ) == 0xD8
                && Byte.toUnsignedInt(
                        data[2]
                ) == 0xFF;
    }


    private boolean isUtf8Text(
            byte[] data,
            int length
    ) {

        for (
                int i = 0;
                i < length;
                i++
        ) {

            if (
                    data[i] == 0
            ) {

                return false;
            }
        }


        try {

            CharBuffer text =
                    StandardCharsets.UTF_8
                            .newDecoder()
                            .onMalformedInput(
                                    CodingErrorAction.REPORT
                            )
                            .onUnmappableCharacter(
                                    CodingErrorAction.REPORT
                            )
                            .decode(
                                    ByteBuffer.wrap(
                                            data,
                                            0,
                                            length
                                    )
                            );


            while (
                    text.hasRemaining()
            ) {

                char character =
                        text.get();


                if (
                        Character.isISOControl(
                                character
                        )
                        && character != '\n'
                        && character != '\r'
                        && character != '\t'
                ) {

                    return false;
                }
            }


            return true;

        } catch (
                CharacterCodingException e
        ) {

            return false;
        }
    }


    private void throwUnsupportedFileType() {

        throw new ResponseStatusException(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Type de fichier non autorisé. Formats acceptés : txt, pdf, png, jpg, jpeg"
        );
    }


    public StoredFile getByDownloadToken(
            String downloadToken
    ) {

        StoredFile storedFile =
                storedFileRepository
                        .findByDownloadToken(
                                downloadToken
                        )
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Token de téléchargement invalide"
                                        )
                        );


        if (
                storedFile
                        .getExpiresAt()
                        .isBefore(
                                LocalDateTime.now()
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Le lien de téléchargement a expiré"
            );
        }


        Path filePath =
                uploadDirectory.resolve(
                        storedFile
                                .getStorageName()
                );


        if (
                !Files.exists(
                        filePath
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Le fichier n'existe plus"
            );
        }


        log.atInfo()
                .addKeyValue(
                        "event",
                        "file_download"
                )
                .addKeyValue(
                        "fileId",
                        storedFile.getId()
                )
                .addKeyValue(
                        "size",
                        storedFile.getSize()
                )
                .log(
                        "File downloaded successfully"
                );


        return storedFile;
    }


    public Path getFilePath(
            StoredFile storedFile
    ) {

        return uploadDirectory.resolve(
                storedFile
                        .getStorageName()
        );
    }


    public FileHistoryPageResponse getHistory(
            String email,
            int page,
            int size
    ) {

        if (page < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le numéro de page ne peut pas être négatif"
            );
        }


        if (
                size < 1
                || size > MAX_PAGE_SIZE
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La taille de page doit être comprise entre 1 et 50"
            );
        }


        Page<StoredFile> result =
                storedFileRepository
                        .findByOwnerEmailOrderByCreatedAtDesc(
                                email,
                                PageRequest.of(
                                        page,
                                        size
                                )
                        );


        List<FileHistoryResponse> content =
                result.getContent()
                        .stream()
                        .map(
                                storedFile ->
                                        new FileHistoryResponse(
                                                storedFile.getId(),
                                                storedFile.getOriginalName(),
                                                storedFile.getSize(),
                                                storedFile.getContentType(),
                                                storedFile.getDownloadToken(),
                                                storedFile.getCreatedAt(),
                                                storedFile.getExpiresAt()
                                        )
                        )
                        .toList();


        return new FileHistoryPageResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }


    public void deleteFile(
            Long id,
            String email
    ) {

        StoredFile storedFile =
                storedFileRepository
                        .findByIdAndOwnerEmail(
                                id,
                                email
                        )
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Fichier introuvable"
                                        )
                        );


        Path filePath =
                uploadDirectory.resolve(
                        storedFile
                                .getStorageName()
                );


        try {

            Files.deleteIfExists(
                    filePath
            );

        } catch (IOException e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Impossible de supprimer le fichier"
            );
        }


        storedFileRepository.delete(
                storedFile
        );


        log.atInfo()
                .addKeyValue(
                        "event",
                        "file_delete"
                )
                .addKeyValue(
                        "fileId",
                        storedFile.getId()
                )
                .addKeyValue(
                        "owner",
                        storedFile
                                .getOwner()
                                .getEmail()
                )
                .log(
                        "File deleted successfully"
                );
    }
}