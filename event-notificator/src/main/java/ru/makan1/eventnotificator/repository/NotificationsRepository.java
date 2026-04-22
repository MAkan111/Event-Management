package ru.makan1.eventnotificator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.makan1.eventnotificator.entity.Notifications;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Long> {
    boolean existsByUserIdAndPayloadId(Long userId, Long payloadId);
    List<Notifications> findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    long deleteByCreatedAtBefore(Instant threshold);

    long countByUserIdAndNotificationIdIn(Long userId, Collection<Long> notificationIds);

    @Modifying
    @Transactional
    @Query("""
            update Notifications n
               set n.isRead = true,
                   n.readAt = :readAt
             where n.userId = :userId
               and n.notificationId in :notificationIds
               and n.isRead = false
            """)
    int markAsRead(Long userId, Collection<Long> notificationIds, Instant readAt);
}
