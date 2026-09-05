package com.naveen.document_service.service;

import com.naveen.document_service.exception.InvalidFileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
public class FileValidationService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES =
            Set.of(
                    "application/pdf",
                    "image/jpeg",
                    "image/png"
            );

    public void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidFileException(
                    "File must not be empty"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                    "File size must not exceed 5 MB"
            );
        }

        try {

            byte[] bytes = file.getBytes();

            if (!isValidFileSignature(bytes)) {
                throw new InvalidFileException(
                        "Only valid PDF, JPG, JPEG and PNG files are allowed"
                );
            }

        } catch (IOException e) {

            throw new InvalidFileException(
                    "Unable to read uploaded file"
            );
        }
    }

    private boolean isValidFileSignature(byte[] bytes) {

        return isPng(bytes)
                || isJpeg(bytes)
                || isPdf(bytes);
    }

    private boolean isPng(byte[] bytes) {

        return bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && (bytes[1] & 0xFF) == 0x50
                && (bytes[2] & 0xFF) == 0x4E
                && (bytes[3] & 0xFF) == 0x47
                && (bytes[4] & 0xFF) == 0x0D
                && (bytes[5] & 0xFF) == 0x0A
                && (bytes[6] & 0xFF) == 0x1A
                && (bytes[7] & 0xFF) == 0x0A;
    }

    private boolean isJpeg(byte[] bytes) {

        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }

    private boolean isPdf(byte[] bytes) {

        return bytes.length >= 4
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F';
    }
}