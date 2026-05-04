package ru.makan1.eventmanager.config;

import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.makan1.eventcommon.messages.EventKafkaMessage;

import java.util.UUID;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<UUID, EventKafkaMessage> kafkaTemplate(KafkaProperties kafkaProperties) {

        var props = kafkaProperties.buildProducerProperties();

        ProducerFactory<UUID, EventKafkaMessage> producerFactory = new DefaultKafkaProducerFactory<>(props);

        return new KafkaTemplate<>(producerFactory);
    }
}
