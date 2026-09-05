package com.naveen.document_service.dto;

import com.naveen.document_service.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getCustomerId(),
                document.getLoanId(),
                document.getDocumentType(),
                document.getFileName(),
                document.getFileType(),
                document.getFileSize(),
                document.getCloudinaryUrl(),
                document.getStatus(),
                document.getUploadedAt(),
                document.getVerifiedAt()
        );
    }
}
