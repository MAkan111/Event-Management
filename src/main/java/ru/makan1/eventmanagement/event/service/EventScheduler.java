package ru.makan1.eventmanagement.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.makan1.eventmanagement.event.entity.EventEntity;
import ru.makan1.eventmanagement.event.enums.EventStatus;
import ru.makan1.eventmanagement.event.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final EventRepository eventRepository;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void updateEventStatuses() {
        LocalDateTime now = LocalDateTime.now();

        List<EventEntity> toStart = eventRepository.findByStatus(EventStatus.WAIT_START);
        for (EventEntity event : toStart) {
            if (event.getDate() != null && !event.getDate().isAfter(now)) {
                event.setStatus(EventStatus.STARTED);
                log.info("Мероприятие id={} переведено в статус STARTED", event.getId());
            }
        }
        eventRepository.saveAll(toStart.stream()
                .filter(e -> e.getStatus() == EventStatus.STARTED)
                .toList());

        List<EventEntity> toFinish = eventRepository.findByStatus(EventStatus.STARTED);
        for (EventEntity event : toFinish) {
            if (event.getDate() != null) {
                LocalDateTime endTime = event.getDate().plusMinutes(event.getDuration());
                if (!endTime.isAfter(now)) {
                    event.setStatus(EventStatus.FINISHED);
                    log.info("Мероприятие id={} переведено в статус FINISHED", event.getId());
                }
            }
        }
        eventRepository.saveAll(toFinish.stream()
                .filter(e -> e.getStatus() == EventStatus.FINISHED)
                .toList());
    }
}
