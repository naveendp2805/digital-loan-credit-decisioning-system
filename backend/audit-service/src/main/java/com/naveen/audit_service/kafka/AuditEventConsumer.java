package com.naveen.audit_service.kafka;

import com.naveen.audit_service.dto.AuditEvent;
import com.naveen.audit_service.entity.AuditLog;
import com.naveen.audit_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventConsumer {

    private final AuditLogService auditLogService;


    @KafkaListener(
            topics = "audit-events",
            groupId = "audit-service-group",
            containerFactory = "auditKafkaListenerContainerFactory"
    )
    public void consumeAuditEvent() {
        consumeAuditEvent(null);
    }

    @KafkaListener(
            topics = "audit-events",
            groupId = "audit-service-group",
            containerFactory = "auditKafkaListenerContainerFactory"
    )
    public void consumeAuditEvent(AuditEvent event) {

        log.info(
                "Received audit event: eventId={}, action={}, resourceType={}, resourceId={}",
                event.getEventId(),
                event.getAction(),
                event.getResourceType(),
                event.getResourceId()
        );

        if (event.getEventId() == null || event.getEventId().isBlank()) {
            throw new IllegalArgumentException("Audit eventId cannot be null or blank");
        }

        if (auditLogService.existsByEventId(event.getEventId())) {
            log.info("Duplicate audit event ignored: {}", event.getEventId());
        } else {

            AuditLog auditLog = AuditLog.builder()
                            .eventId(event.getEventId())
                            .actorId(event.getActorId())
                            .actorEmail(event.getActorEmail())
                            .actorRole(event.getActorRole())
                            .action(event.getAction())
                            .resourceType(event.getResourceType())
                            .resourceId(event.getResourceId())
                            .description(event.getDescription())
                            .status(event.getStatus())
                            .ipAddress(event.getIpAddress())
                            .correlationId(event.getCorrelationId())
                            .timestamp(event.getTimestamp())
                            .build();

            auditLogService.createAuditLog(auditLog);

            log.info("Audit event persisted successfully: {}", event.getEventId());
        }
    }
}
