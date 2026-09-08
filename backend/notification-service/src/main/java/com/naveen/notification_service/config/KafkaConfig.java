package com.naveen.notification_service.config;

import com.naveen.notification_service.event.LoanApprovedEvent;
import com.naveen.notification_service.event.PaymentSuccessEvent;
import com.naveen.notification_service.event.RepaymentDueEvent;
import com.naveen.notification_service.exception.CustomerNotFoundException;
import com.naveen.notification_service.exception.LoanNotFoundException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.mail.MailException;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private Map<String, Object> consumerProperties() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

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

        JacksonJsonDeserializer<PaymentSuccessEvent> deserializer = new JacksonJsonDeserializer<>(PaymentSuccessEvent.class);

        deserializer.addTrustedPackages("com.naveen.notification_service.event");

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

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {

        FixedBackOff fixedBackOff = new FixedBackOff(0L, 0L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(fixedBackOff);

        errorHandler.addNotRetryableExceptions(
                CustomerNotFoundException.class,
                LoanNotFoundException.class,
                IllegalArgumentException.class,
                MailException.class
        );

        return errorHandler;
    }
}