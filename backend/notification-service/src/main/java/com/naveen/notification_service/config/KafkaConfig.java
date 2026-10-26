package com.naveen.notification_service.config;

import com.naveen.notification_service.event.LoanApprovedEvent;
import com.naveen.notification_service.event.PaymentSuccessEvent;
import com.naveen.notification_service.event.RepaymentDueEvent;
import com.naveen.notification_service.exception.CustomerNotFoundException;
import com.naveen.notification_service.exception.LoanNotFoundException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}")
    private String bootstrapServers;

    private Map<String, Object> consumerProperties() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service");

        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);

        return properties;
    }

    @Bean
    public ConsumerFactory<String, LoanApprovedEvent> loanApprovedConsumerFactory() {

        JacksonJsonDeserializer<LoanApprovedEvent> deserializer = new JacksonJsonDeserializer<>(LoanApprovedEvent.class);

        deserializer.addTrustedPackages("com.naveen.notification_service.event");

        return new DefaultKafkaConsumerFactory<>(consumerProperties(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConsumerFactory<String, PaymentSuccessEvent> paymentSuccessConsumerFactory() {

        JacksonJsonDeserializer<PaymentSuccessEvent> deserializer =
                new JacksonJsonDeserializer<>(PaymentSuccessEvent.class);

        deserializer.addTrustedPackages(
                "com.naveen.notification_service.event"
        );

        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(consumerProperties(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConsumerFactory<String, RepaymentDueEvent> repaymentDueConsumerFactory() {

        JacksonJsonDeserializer<RepaymentDueEvent> deserializer = new JacksonJsonDeserializer<>(RepaymentDueEvent.class);

        deserializer.addTrustedPackages("com.naveen.notification_service.event");

        return new DefaultKafkaConsumerFactory<>(consumerProperties(), new StringDeserializer(), deserializer);
    }

    @Bean(name = "loanApprovedKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, LoanApprovedEvent> loanApprovedKafkaListenerContainerFactory(DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, LoanApprovedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(loanApprovedConsumerFactory());
        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }

    @Bean(name = "paymentSuccessKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessEvent> paymentSuccessKafkaListenerContainerFactory(DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(paymentSuccessConsumerFactory());
        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }

    @Bean(name = "repaymentDueKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, RepaymentDueEvent> repaymentDueKafkaListenerContainerFactory(DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, RepaymentDueEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(repaymentDueConsumerFactory());
        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }

    @Bean(name = "dltProducerFactory")
    public ProducerFactory<String, Object> dltProducerFactory() {
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
                JsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean(name = "dltKafkaTemplate")
    public KafkaTemplate<String, Object> dltKafkaTemplate() {
        return new KafkaTemplate<>(dltProducerFactory());
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            @org.springframework.beans.factory.annotation.Qualifier("dltKafkaTemplate")
            KafkaTemplate<String, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(
                                        record.topic() + ".DLT",
                                        0
                                )
                );

        FixedBackOff fixedBackOff = new FixedBackOff(2000L, 3L);

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, fixedBackOff);

        errorHandler.addNotRetryableExceptions(
                CustomerNotFoundException.class,
                LoanNotFoundException.class,
                IllegalArgumentException.class
        );

        return errorHandler;
    }
}