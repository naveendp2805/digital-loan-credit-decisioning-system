package com.naveen.audit_service.repository;

import com.naveen.audit_service.entity.AuditAction;
import com.naveen.audit_service.entity.AuditLog;
import com.naveen.audit_service.entity.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByActorId(Long actorId);

    List<AuditLog> findByAction(AuditAction action);

    List<AuditLog> findByResourceTypeAndResourceId(ResourceType resourceType, String resourceId);

    List<AuditLog> findByActorEmail(String actorEmail);

    boolean existsByEventId(String eventId);
}
