package ru.makan1.eventmanager.event.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.makan1.eventcommon.messages.EventKafkaMessage;

import java.util.UUID;

@Service
@Slf4j
public class EventSender {
    private final KafkaTemplate<UUID, EventKafkaMessage> kafkaTemplate;

    public EventSender(KafkaTemplate<UUID, EventKafkaMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(EventKafkaMessage eventKafkaMessage) {
        log.info("Sending event: event={}", eventKafkaMessage);

        var result = kafkaTemplate.send(
                "event-topic",
                eventKafkaMessage.getMessageId(),
                eventKafkaMessage
        );

        result.thenAccept(sendResult -> {
            log.info("Event sent successfully");
        });
    }
}
