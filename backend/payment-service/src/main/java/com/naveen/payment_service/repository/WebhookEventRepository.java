package com.naveen.payment_service.repository;

import com.naveen.payment_service.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    Optional<WebhookEvent> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}