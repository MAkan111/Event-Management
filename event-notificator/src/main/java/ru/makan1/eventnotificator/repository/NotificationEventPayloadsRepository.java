package ru.makan1.eventnotificator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.makan1.eventnotificator.entity.NotificationEventPayloads;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationEventPayloadsRepository extends JpaRepository<NotificationEventPayloads, Long> {
    Optional<NotificationEventPayloads> findByMessageId(UUID messageId);
    List<NotificationEventPayloads> findAllByPayloadIdIn(Collection<Long> payloadIds);

    @Modifying
    @Query(
            value = """
                    DELETE FROM notification_event_payloads nep
                    WHERE NOT exists (
                        SELECT 1
                        FROM notifications n
                        WHERE n.payload_id = nep.payload_id
                    )
                    """,
            nativeQuery = true
    )
    long deleteOrphanPayloads();
}
