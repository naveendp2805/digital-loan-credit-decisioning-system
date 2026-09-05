package com.naveen.document_service.dto;

import com.naveen.document_service.entity.DocumentStatus;
import com.naveen.document_service.entity.DocumentType;

import java.time.LocalDateTime;

public record DocumentResponse(

        Long id,

        Long customerId,

        Long loanId,

        DocumentType documentType,

        String fileName,

        String fileType,

        Long fileSize,

        String documentUrl,

        DocumentStatus status,

        LocalDateTime uploadedAt,

        LocalDateTime verifiedAt

) {
}