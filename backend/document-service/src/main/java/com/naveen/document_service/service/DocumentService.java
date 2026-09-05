package com.naveen.document_service.service;

import com.naveen.document_service.dto.DocumentMapper;
import com.naveen.document_service.dto.DocumentResponse;
import com.naveen.document_service.entity.Document;
import com.naveen.document_service.entity.DocumentStatus;
import com.naveen.document_service.entity.DocumentType;
import com.naveen.document_service.exception.DocumentNotFoundException;
import com.naveen.document_service.repository.DocumentRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CloudinaryService cloudinaryService;
    private final FileValidationService fileValidationService;
    private final DocumentMapper mapper;

    public DocumentResponse uploadDocument(Long customerId, Long loanId, DocumentType documentType, MultipartFile file) throws IOException {

        fileValidationService.validate(file);

        CloudinaryService.CloudinaryUploadResult cloudinaryUploadResult = cloudinaryService.upload(file);

        Document document = Document.builder()
                .customerId(customerId)
                .loanId(loanId)
                .documentType(documentType)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .cloudinaryPublicId(cloudinaryUploadResult.publicId())
                .cloudinaryUrl(cloudinaryUploadResult.secureUrl())
                .status(DocumentStatus.UPLOADED)
                .uploadedAt(LocalDateTime.now())
                .build();

        Document savedDocument = documentRepository.save(document);

        return mapper.toResponse(savedDocument);
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        return mapper.toResponse(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByCustomerId(Long customerId) {
        return documentRepository.findByCustomerId(customerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByLoanId(Long loanid) {
        return documentRepository.findByLoanId(loanid)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public String deleteDocument(Long id) throws IOException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        cloudinaryService.delete(document.getCloudinaryPublicId());

        documentRepository.delete(document);

        return "Document with ID: " + id + " deleted Successfully";
    }
}
