package ru.makan1.eventmanagement.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.makan1.eventmanagement.event.entity.EventEntity;
import ru.makan1.eventmanagement.event.enums.EventStatus;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {

    List<EventEntity> findByOwnerId(Long ownerId);

    List<EventEntity> findByStatus(EventStatus status);
}
