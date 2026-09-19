package com.naveen.document_service.service;

import com.naveen.document_service.client.CustomerClient;
import com.naveen.document_service.client.LoanClient;
import com.naveen.document_service.dto.CustomerResponse;
import com.naveen.document_service.dto.DocumentMapper;
import com.naveen.document_service.dto.DocumentResponse;
import com.naveen.document_service.entity.Document;
import com.naveen.document_service.entity.DocumentStatus;
import com.naveen.document_service.entity.DocumentType;
import com.naveen.document_service.entity.LoanResponse;
import com.naveen.document_service.exception.CustomerNotFoundException;
import com.naveen.document_service.exception.DocumentNotFoundException;
import com.naveen.document_service.exception.InvalidFileException;
import com.naveen.document_service.exception.LoanNotFoundException;
import com.naveen.document_service.repository.DocumentRepository;
import feign.FeignException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.jaas.AuthorityGranter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    private final ExternalValidationService externalValidationService;
    private final CustomerClient customerClient;
    private final LoanClient loanClient;

    public DocumentResponse uploadDocument(Long customerId, Long loanId, DocumentType documentType, MultipartFile file, Authentication authentication) throws IOException {

        authorizeCustomerAccess(customerId, authentication);
        authorizeLoanAccess(loanId, authentication);

        externalValidationService.validateCustomer(customerId);
        externalValidationService.validateLoan(loanId);
        externalValidationService.validateLoanOwnership(customerId, loanId);

        if(documentRepository.existsByLoanIdAndDocumentType(loanId, documentType))
            throw new InvalidFileException("This document type already exists for the loan");

        fileValidationService.validate(file);

        CloudinaryService.CloudinaryUploadResult cloudinaryUploadResult = cloudinaryService.upload(file);

        try {
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
        } catch(Exception e) {
            cloudinaryService.delete(cloudinaryUploadResult.publicId());

            throw e;
        }
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long id, Authentication authentication) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        authorizeCustomerAccess(document.getCustomerId(), authentication);

        return mapper.toResponse(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByCustomerId(Long customerId, Authentication authentication) {
        authorizeCustomerAccess(customerId, authentication);

        return documentRepository.findByCustomerId(customerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByLoanId(Long loanId, Authentication authentication) {
        authorizeLoanAccess(loanId, authentication);

        return documentRepository.findByLoanId(loanId)
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

    private void authorizeCustomerAccess(Long customerId, Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (isAdmin) {
            return;
        }

        String authenticatedEmail =
                authentication.getName();

        CustomerResponse customer;

        try {

            customer = customerClient.getCustomerById(customerId);

        } catch (FeignException.NotFound e) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        } catch (FeignException e) {
            throw new CustomerNotFoundException("Customer service is unavailable");
        }

        if (!authenticatedEmail.equalsIgnoreCase(customer.getEmail())) {
            throw new AccessDeniedException("You are not authorized to access this customer's resources");
        }
    }

    private void authorizeLoanAccess(Long loanId, Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (isAdmin) {
            return;
        }

        LoanResponse loan;

        try {
            loan = loanClient.getLoanById(loanId);
        } catch (FeignException.NotFound e) {
            throw new LoanNotFoundException("Loan not found with ID: " + loanId);
        } catch (FeignException e) {
            throw new LoanNotFoundException("Loan service is unavailable");
        }

        authorizeCustomerAccess(loan.customerId(), authentication);
    }
}
