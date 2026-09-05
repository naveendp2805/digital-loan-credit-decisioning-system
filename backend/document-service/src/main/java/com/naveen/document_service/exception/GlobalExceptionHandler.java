package com.naveen.document_service.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<?> handleDocumentNotFound(
            DocumentNotFoundException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<?> handleInvalidFile(
            InvalidFileException ex
    ) {

        return ResponseEntity
                .badRequest()
                .body(error(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<?> handleStorageException(
            FileStorageException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSize() {

        return ResponseEntity
                .status(HttpStatus.CONTENT_TOO_LARGE)
                .body(error(
                        HttpStatus.CONTENT_TOO_LARGE.value(),
                        "File size exceeds the allowed limit"
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(
            ConstraintViolationException ex
    ) {

        return ResponseEntity
                .badRequest()
                .body(error(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage()
                ));
    }

    private Map<String, Object> error(
            int status,
            String message
    ) {

        return Map.of(
                "timestamp",
                LocalDateTime.now(),
                "status",
                status,
                "message",
                message
        );
    }
}