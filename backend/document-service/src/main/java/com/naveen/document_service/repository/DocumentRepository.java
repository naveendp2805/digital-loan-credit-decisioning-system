package com.naveen.document_service.repository;

import com.naveen.document_service.entity.Document;
import com.naveen.document_service.entity.DocumentStatus;
import com.naveen.document_service.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByCustomerId(Long customerId);

    List<Document> findByLoanId(Long loanId);

    List<Document> findByLoanIdAndStatus(Long loanId, DocumentStatus status);

    boolean existsByLoanIdAndDocumentType(Long loanId, DocumentType documentType);
}
