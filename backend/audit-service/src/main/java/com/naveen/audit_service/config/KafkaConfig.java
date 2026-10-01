package com.naveen.audit_service.config;

import com.naveen.audit_service.dto.AuditEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    @Bean
    public NewTopic auditEventsTopic() {
        return new NewTopic("audit-events", 3, (short) 1);
    }

    @Bean
    public NewTopic auditEventsDltTopic() {
        return new NewTopic("audit-events.DLT", 3, (short) 1);
    }

    @Bean
    public ConsumerFactory<String, AuditEvent> auditConsumerFactory() {

        JacksonJsonDeserializer<AuditEvent> deserializer = new JacksonJsonDeserializer<>(AuditEvent.class);

        deserializer.addTrustedPackages("com.naveen.audit_service.dto");

        deserializer.setUseTypeHeaders(false);

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "audit-service-group"
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                deserializer
        );
    }

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
    public KafkaTemplate<String, AuditEvent> auditKafkaTemplate(ProducerFactory<String, AuditEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, AuditEvent> kafkaTemplate) {

        return new DeadLetterPublishingRecoverer(kafkaTemplate, (record, exception) ->
                        new TopicPartition(record.topic() + ".DLT", record.partition()));
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(DeadLetterPublishingRecoverer recoverer) {

        FixedBackOff backOff = new FixedBackOff(1000L, 2L);

        return new DefaultErrorHandler(recoverer, backOff);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AuditEvent> auditKafkaListenerContainerFactory(ConsumerFactory<String, AuditEvent> consumerFactory, DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, AuditEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }
}