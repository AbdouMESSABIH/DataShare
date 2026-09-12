package com.datashare.backend.service;

import com.datashare.backend.dto.UploadResponse;
import com.datashare.backend.entity.StoredFile;
import com.datashare.backend.entity.User;
import com.datashare.backend.repository.StoredFileRepository;
import com.datashare.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


@Service
public class FileService {

    private static final long MAX_FILE_SIZE =
            1024L * 1024 * 1024;

    private static final int MAX_EXPIRATION_DAYS = 7;

    private static final Set<String> BLOCKED_EXTENSIONS =
            Set.of("exe", "bat");


    private final StoredFileRepository storedFileRepository;

    private final UserRepository userRepository;

    private final Path uploadDirectory =
            Paths.get("uploads");


    public FileService(
            StoredFileRepository storedFileRepository,
            UserRepository userRepository
    ) {

        this.storedFileRepository = storedFileRepository;

        this.userRepository = userRepository;


        try {

            Files.createDirectories(uploadDirectory);

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


        if (file.getSize() > MAX_FILE_SIZE) {

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
                StringUtils.getFilenameExtension(originalName);


        if (extension != null
                && BLOCKED_EXTENSIONS.contains(
                        extension.toLowerCase(Locale.ROOT)
                )) {

            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Ce type de fichier est interdit"
            );
        }


        int days = expirationDays == null
                ? MAX_EXPIRATION_DAYS
                : expirationDays;


        if (days < 1 || days > MAX_EXPIRATION_DAYS) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La durée d'expiration doit être comprise entre 1 et 7 jours"
            );
        }


        User owner =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Utilisateur introuvable"
                                )
                        );


        String storageName =
                UUID.randomUUID()
                        + (extension == null
                        ? ""
                        : "." + extension);


        String downloadToken =
                UUID.randomUUID().toString();


        LocalDateTime createdAt =
                LocalDateTime.now();

        LocalDateTime expiresAt =
                createdAt.plusDays(days);


        Path targetPath =
                uploadDirectory.resolve(storageName);


        try {

            file.transferTo(targetPath);

        } catch (IOException e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Impossible d'enregistrer le fichier"
            );
        }


        StoredFile storedFile =
                new StoredFile(
                        originalName,
                        storageName,
                        file.getContentType(),
                        file.getSize(),
                        downloadToken,
                        createdAt,
                        expiresAt,
                        owner
                );


        StoredFile savedFile =
                storedFileRepository.save(storedFile);


        return new UploadResponse(
                savedFile.getId(),
                savedFile.getOriginalName(),
                savedFile.getSize(),
                savedFile.getDownloadToken(),
                savedFile.getExpiresAt()
        );
    }


    public StoredFile getByDownloadToken(
            String downloadToken
    ) {

        StoredFile storedFile =
                storedFileRepository
                        .findByDownloadToken(downloadToken)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Token de téléchargement invalide"
                                )
                        );


        if (storedFile.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Le lien de téléchargement a expiré"
            );
        }


        Path filePath =
                uploadDirectory.resolve(
                        storedFile.getStorageName()
                );


        if (!Files.exists(filePath)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Le fichier n'existe plus"
            );
        }


        return storedFile;
    }


    public Path getFilePath(
            StoredFile storedFile
    ) {

        return uploadDirectory.resolve(
                storedFile.getStorageName()
        );
    }
}