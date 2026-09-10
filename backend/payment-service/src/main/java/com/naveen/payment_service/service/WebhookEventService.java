package com.naveen.payment_service.service;

import com.naveen.payment_service.entity.WebhookEvent;
import com.naveen.payment_service.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WebhookEventService {

    private final WebhookEventRepository webhookEventRepository;

    public boolean alreadyProcessed(String eventId) {

        return webhookEventRepository
                .findByEventId(eventId)
                .map(WebhookEvent::isProcessed)
                .orElse(false);
    }

    public void registerEvent(
            String eventId,
            String eventType
    ) {

        if (webhookEventRepository.existsByEventId(eventId)) {
            return;
        }

        WebhookEvent event = WebhookEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .processed(false)
                .receivedAt(LocalDateTime.now())
                .build();

        webhookEventRepository.save(event);
    }

    public void markProcessed(String eventId) {

        WebhookEvent event = webhookEventRepository
                .findByEventId(eventId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Webhook event not found: " + eventId
                        )
                );

        event.setProcessed(true);
        event.setProcessedAt(LocalDateTime.now());

        webhookEventRepository.save(event);
    }
}