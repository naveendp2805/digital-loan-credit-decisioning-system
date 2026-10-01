package com.naveen.audit_service.service;

import com.naveen.audit_service.entity.AuditLog;
import com.naveen.audit_service.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLog createAuditLog(AuditLog auditLog) {

        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(Instant.now());
        }

        return auditLogRepository.save(auditLog);
    }

    public boolean existsByEventId(String eventId) {
        return auditLogRepository.existsByEventId(eventId);
    }
}
