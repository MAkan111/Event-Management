package ru.makan1.eventnotificator.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.makan1.eventcommon.messages.EventKafkaMessage;

import java.util.UUID;

@Slf4j
@Component
public class EventKafkaListener {

    private final NotificationsService notificationsService;

    public EventKafkaListener(NotificationsService notificationsService) {
        this.notificationsService = notificationsService;
    }

    @KafkaListener(topics = "event-topic")
    public void listenEvents(ConsumerRecord<UUID, EventKafkaMessage> record) {
        try {
            log.info("Received event from topic: {}", record.value());
            notificationsService.processMessage(record.value());
        } catch (Exception ex) {
            // не роняем consumer из-за одного плохого сообщения
            log.error("Failed to process Kafka messageId={}", record.key(), ex);
        }
    }
}
