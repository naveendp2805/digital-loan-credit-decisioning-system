package com.naveen.audit_common.dto;

import com.naveen.audit_common.enums.AuditAction;
import com.naveen.audit_common.enums.AuditStatus;
import com.naveen.audit_common.enums.ResourceType;
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
