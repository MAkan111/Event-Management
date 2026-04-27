package ru.makan1.eventnotificator.service;

import org.springframework.stereotype.Service;
import ru.makan1.eventcommon.messages.EventKafkaMessage;
import ru.makan1.eventnotificator.entity.NotificationEventPayloads;
import ru.makan1.eventnotificator.repository.NotificationEventPayloadsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class NotificationEventPayloadsService {

    private final NotificationEventPayloadsRepository notificationEventPayloadsRepository;
    private final ObjectMapper objectMapper;

    public NotificationEventPayloadsService(NotificationEventPayloadsRepository notificationEventPayloadsRepository,
                                            ObjectMapper objectMapper
    ) {
        this.notificationEventPayloadsRepository = notificationEventPayloadsRepository;
        this.objectMapper = objectMapper;
    }

    public Long getOrCreatePayloadId(EventKafkaMessage message) {
        Optional<NotificationEventPayloads> existing = notificationEventPayloadsRepository.findByMessageId(message.getMessageId());
        if (existing.isPresent()) {
            return existing.get().getPayloadId();
        }

        NotificationEventPayloads payload = new NotificationEventPayloads();
        payload.setMessageId(message.getMessageId());
        payload.setEventType(message.getEventType().name());
        payload.setEventId(message.getEventId());
        payload.setOccurredAt(message.getOccurredAt());
        payload.setChangedById(message.getChangedById());
        payload.setOwnerId(message.getOwnerId());

        Map<String, Object> structuredPayload = new LinkedHashMap<>();
        structuredPayload.put("changedById", message.getChangedById());
        structuredPayload.put("changes", message.getChanges());
        JsonNode node = objectMapper.valueToTree(structuredPayload);
        payload.setPayload(node);

        return notificationEventPayloadsRepository.save(payload).getPayloadId();
    }
}
