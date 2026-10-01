package com.naveen.audit_service.dto;

import com.naveen.audit_service.entity.AuditAction;
import com.naveen.audit_service.entity.AuditStatus;
import com.naveen.audit_service.entity.ResourceType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    private String eventId;

    private Long actorId;

    private String actorEmail;

    private String actorRole;

    private AuditAction action;

    private ResourceType resourceType;

    private String resourceId;

    private String description;

    private AuditStatus status;

    private String ipAddress;

    private String correlationId;

    private Instant timestamp;
}
