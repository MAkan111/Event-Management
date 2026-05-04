package ru.makan1.eventcommon.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.makan1.eventcommon.enums.EventType;
import ru.makan1.eventcommon.model.ChangeItem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventKafkaMessage {
    private UUID messageId;
    private EventType eventType;
    private Long eventId;
    private Instant occurredAt;
    private Long ownerId;
    private Long changedById;
    private List<Long> subscribers;
    private List<ChangeItem> changes;
}
