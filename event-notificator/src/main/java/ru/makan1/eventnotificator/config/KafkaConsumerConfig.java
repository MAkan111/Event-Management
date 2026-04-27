package ru.makan1.eventnotificator.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.makan1.eventcommon.messages.EventKafkaMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<UUID, EventKafkaMessage> consumerFactory(
            ObjectMapper objectMapper,
            @Value("${spring.kafka.consumer.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, UUIDDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        var keyDeserializer = new UUIDDeserializer();

        var valueDeserializer = new JsonDeserializer<>(EventKafkaMessage.class, objectMapper, false);
        valueDeserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(props, keyDeserializer, valueDeserializer);
    }

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<UUID, EventKafkaMessage> kafkaListenerContainerFactory(
            ConsumerFactory<UUID, EventKafkaMessage> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<UUID, EventKafkaMessage>();
        factory.setConsumerFactory(consumerFactory);

        var backOff = new ExponentialBackOffWithMaxRetries(0);
        factory.setCommonErrorHandler(new DefaultErrorHandler(backOff));

        return factory;
    }
}

