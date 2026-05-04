package ru.makan1.eventnotificator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "notifications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notifications_user_payload", columnNames = {"user_id", "payload_id"})
        },
        indexes = {
                @Index(name = "idx_notifications_user_read_created", columnList = "user_id,is_read,created_at")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    Long notificationId;

    @Column(name = "user_id")
    Long userId;

    @Column(name = "payload_id")
    Long payloadId;

    @Column(name = "is_read", nullable = false)
    Boolean isRead;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "read_at")
    Instant readAt;
}
