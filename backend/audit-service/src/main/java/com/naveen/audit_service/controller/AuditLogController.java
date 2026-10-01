package com.naveen.audit_service.controller;

import com.naveen.audit_service.entity.AuditLog;
import com.naveen.audit_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audits")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<AuditLog> createAuditLog(@RequestBody AuditLog auditLog) {
        AuditLog savedAuditLog = auditLogService.createAuditLog(auditLog);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedAuditLog);
    }
}
