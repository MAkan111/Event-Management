package ru.makan1.eventnotificator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
