package ru.makan1.eventnotificator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "notification_event_payloads",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notification_event_payloads_message_id", columnNames = {"message_id"})
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NotificationEventPayloads {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payload_id")
    Long payloadId;

    @Column(name = "message_id", nullable = false)
    UUID messageId;

    @Column(name = "event_type", nullable = false)
    String eventType;

    @Column(name = "event_id", nullable = false)
    Long eventId;

    @Column(name = "occurred_at", nullable = false)
    Instant occurredAt;

    @Column(name = "changed_by_id")
    Long changedById;

    @Column(name = "owner_id", nullable = false)
    Long ownerId;

    @Column(name = "payload", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode payload;
}
