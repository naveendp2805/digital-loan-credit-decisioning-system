package com.naveen.audit_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(
                        name = "idx_audit_actor_id",
                        columnList = "actor_id"
                ),
                @Index(
                        name = "idx_audit_action",
                        columnList = "action"
                ),
                @Index(
                        name = "idx_audit_resource",
                        columnList = "resource_type, resource_id"
                ),
                @Index(
                        name = "idx_audit_timestamp",
                        columnList = "timestamp"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;


    @Column(name = "actor_id")
    private Long actorId;


    @Column(name = "actor_email")
    private String actorEmail;


    @Column(name = "actor_role")
    private String actorRole;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;


    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;


    @Column(name = "resource_id")
    private String resourceId;


    @Column(length = 1000)
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditStatus status;


    @Column(name = "ip_address")
    private String ipAddress;


    @Column(name = "correlation_id")
    private String correlationId;


    @Column(nullable = false)
    private Instant timestamp;
}