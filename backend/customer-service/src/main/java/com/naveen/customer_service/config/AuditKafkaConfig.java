package com.naveen.customer_service.config;

import com.naveen.audit_common.dto.AuditEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AuditKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, AuditEvent> auditProducerFactory() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, AuditEvent> auditKafkaTemplate(
            ProducerFactory<String, AuditEvent> auditProducerFactory) {

        return new KafkaTemplate<>(auditProducerFactory);
    }

    @Bean
    public com.naveen.audit_common.kafka.AuditEventPublisher auditEventPublisher(
            KafkaTemplate<String, AuditEvent> auditKafkaTemplate) {

        return new com.naveen.audit_common.kafka.AuditEventPublisher(
                auditKafkaTemplate
        );
    }
}