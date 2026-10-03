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

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.HttpStatus;

import org.springframework.security.crypto.password.PasswordEncoder;

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
                    "jpeg", "image/jpeg",
                    "mp3", "audio/mpeg",
                    "mp4", "video/mp4",
                    "zip", "application/zip"
            );


    private final StoredFileRepository
            storedFileRepository;


    private final UserRepository
            userRepository;


    private final PasswordEncoder
            passwordEncoder;


    private final Path uploadDirectory =
            Paths.get("uploads");


    @Autowired
    public FileService(
            StoredFileRepository storedFileRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {

        this.storedFileRepository =
                storedFileRepository;

        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;
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
            Integer expirationDays,
            String password
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


        // Un mot de passe est facultatif.
        // S'il est renseigné, il doit avoir au moins 6 caractères utiles.
        if (password != null && !password.isBlank()) {

            String normalizedPassword = password.strip();

            if (normalizedPassword.codePointCount(
                    0,
                    normalizedPassword.length()
            ) < 6) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Le mot de passe doit contenir au moins 6 caractères"
                );
            }
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


        if (
                password != null
                && !password.isBlank()
        ) {

            storedFile.setPasswordHash(
                    passwordEncoder.encode(
                            password
                    )
            );
        }


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


        if (isZip(sample, bytesRead)) {
            return "application/zip";
        }

        if (isMp4(sample, bytesRead)) {
            return "video/mp4";
        }

        if (isMp3(sample, bytesRead)) {
            return "audio/mpeg";
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


    // Signature ZIP standard ou archive ZIP vide.
    // Le serveur ne décompresse jamais les archives.
    private boolean isZip(byte[] data, int length) {
        if (length < 4 || data[0] != 'P' || data[1] != 'K') {
            return false;
        }

        boolean standardArchive =
                length >= 30 && data[2] == 3 && data[3] == 4;

        boolean emptyArchive =
                length >= 22 && data[2] == 5 && data[3] == 6;

        return standardArchive || emptyArchive;
    }


    // Signature de la boite File Type (ftyp) du conteneur MP4.
    private boolean isMp4(byte[] data, int length) {
        if (length < 16) {
            return false;
        }

        int boxSize = ByteBuffer.wrap(data, 0, 4).getInt();

        return boxSize >= 16
                && boxSize <= length
                && data[4] == 'f'
                && data[5] == 't'
                && data[6] == 'y'
                && data[7] == 'p';
    }


    // MP3 : en-tete ID3v2 ou en-tete MPEG Audio valide.
    private boolean isMp3(byte[] data, int length) {
        if (length >= 10
                && data[0] == 'I'
                && data[1] == 'D'
                && data[2] == '3') {

            int version = Byte.toUnsignedInt(data[3]);

            if (version >= 2 && version <= 4) {
                boolean validSize = true;

                for (int i = 6; i < 10; i++) {
                    if ((data[i] & 0x80) != 0) {
                        validSize = false;
                    }
                }

                if (validSize) {
                    return true;
                }
            }
        }

        if (length < 4) {
            return false;
        }

        int first = Byte.toUnsignedInt(data[0]);
        int second = Byte.toUnsignedInt(data[1]);
        int third = Byte.toUnsignedInt(data[2]);

        int version = (second >> 3) & 3;
        int layer = (second >> 1) & 3;
        int bitrate = (third >> 4) & 15;
        int frequency = (third >> 2) & 3;

        return first == 0xFF
                && (second & 0xE0) == 0xE0
                && version != 1
                && layer != 0
                && bitrate > 0
                && bitrate < 15
                && frequency != 3;
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
                "Type de fichier non autorisé. Formats acceptés : txt, pdf, png, jpg, jpeg, mp3, mp4, zip"
        );
    }


    private StoredFile findValidFile(
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


        return storedFile;
    }


    public StoredFile getByDownloadToken(
            String downloadToken
    ) {

        return findValidFile(
                downloadToken
        );
    }


    public StoredFile getByDownloadToken(
            String downloadToken,
            String password
    ) {

        StoredFile storedFile =
                findValidFile(
                        downloadToken
                );


        String passwordHash =
                storedFile.getPasswordHash();


        if (
                passwordHash != null
                && !passwordHash.isBlank()
        ) {

            if (
                    password == null
                    || password.isBlank()
            ) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Mot de passe requis"
                );
            }


            if (
                    !passwordEncoder.matches(
                            password,
                            passwordHash
                    )
            ) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Mot de passe incorrect"
                );
            }
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
                                                storedFile.getExpiresAt(),
                                                storedFile.getPasswordHash() != null
                                                        && !storedFile.getPasswordHash().isBlank()
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
