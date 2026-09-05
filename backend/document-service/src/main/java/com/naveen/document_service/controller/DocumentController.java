package com.naveen.document_service.controller;

import com.naveen.document_service.dto.DocumentResponse;
import com.naveen.document_service.entity.DocumentType;
import com.naveen.document_service.service.DocumentService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Validated
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam @Positive Long customerId,
                                                           @RequestParam @Positive Long loanId,
                                                           @RequestParam @NotNull DocumentType documentType,
                                                           @RequestPart("file") MultipartFile file) throws IOException {
        DocumentResponse response = documentService.uploadDocument(customerId, loanId, documentType, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long documentId) {
        return ResponseEntity.ok(documentService.getDocument(documentId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<DocumentResponse>> getCustomerDocuments(@PathVariable Long customerId) {
        return ResponseEntity.ok(documentService.getDocumentsByCustomerId(customerId));
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<DocumentResponse>> getLoanDocuments(@PathVariable Long loanId) {
        return ResponseEntity.ok(documentService.getDocumentsByLoanId(loanId));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<String> deleteDocument(@PathVariable Long documentId) throws IOException {
        return ResponseEntity.ok(documentService.deleteDocument(documentId));
    }
}
