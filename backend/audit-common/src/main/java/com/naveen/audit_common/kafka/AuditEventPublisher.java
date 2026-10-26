package com.naveen.audit_common.kafka;

import com.naveen.audit_common.dto.AuditEvent;
import com.naveen.audit_common.enums.AuditAction;
import com.naveen.audit_common.enums.AuditStatus;
import com.naveen.audit_common.enums.ResourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class AuditEventPublisher {

    private static final String AUDIT_TOPIC = "audit-events";

    private final KafkaTemplate<String, AuditEvent> kafkaTemplate;

    public void publish(
            Long actorId,
            String actorEmail,
            String actorRole,
            AuditAction action,
            ResourceType resourceType,
            String resourceId,
            String description,
            AuditStatus status,
            String ipAddress,
            String correlationId
    ) {

        AuditEvent event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .actorId(actorId)
                .actorEmail(actorEmail)
                .actorRole(actorRole)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .description(description)
                .status(status)
                .ipAddress(ipAddress)
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();

        kafkaTemplate.send(
                AUDIT_TOPIC,
                resourceId,
                event
        ).whenComplete((result, exception) -> {
            if (exception != null) {
                System.err.println("Failed to publish audit event: " + event.getEventId());
                return;
            }

            System.out.println("Audit event published successfully: " + event.getEventId() + " | partition=" + result.getRecordMetadata().partition() + " | offset=" + result.getRecordMetadata().offset());
        });
    }
}