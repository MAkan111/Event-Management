package ru.makan1.eventmanager.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.makan1.eventcommon.model.ChangeItem;
import ru.makan1.eventmanager.event.dto.EventResponse;
import ru.makan1.eventmanager.event.entity.EventEntity;
import ru.makan1.eventmanager.event.enums.EventStatus;
import ru.makan1.eventmanager.event.mapper.EventMapper;
import ru.makan1.eventmanager.event.repository.EventRepository;
import ru.makan1.eventmanager.utils.ChangesBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final EventRepository eventRepository;
    private final TxService txService;
    private final EventSender eventSender;

    @Scheduled(fixedRateString = "${spring.scheduler.fixed-rate}")
    public void updateEventStatuses() {
        LocalDateTime now = LocalDateTime.now();

        List<EventEntity> toStart = eventRepository.findByStatus(EventStatus.WAIT_START);

        for (EventEntity event : toStart) {
            if (event.getDate() != null && !event.getDate().isAfter(now)) {
                var changeItem = ChangesBuilder.change("status", event.getStatus().name(), EventStatus.STARTED.name());
                event.setStatus(EventStatus.STARTED);
                EventResponse eventResponse = EventMapper.mapToEventResponse(event);
                var kafkaMessage = EventMapper.toKafkaMessageUpdated(
                        eventResponse,
                        null,
                        collectSubscriberIds(event),
                        List.of(changeItem)
                );
                eventSender.sendEvent(kafkaMessage);
                log.info("Мероприятие id={} переведено в статус STARTED", event.getId());
            }
        }

        txService.saveAllToEventDb(toStart.stream()
                .filter(e -> e.getStatus() == EventStatus.STARTED)
                .toList());

        List<EventEntity> toFinish = eventRepository.findByStatus(EventStatus.STARTED);
        for (EventEntity event : toFinish) {
            if (event.getDate() != null) {
                LocalDateTime endTime = event.getDate().plusMinutes(event.getDuration());
                if (!endTime.isAfter(now)) {
                    var changeItem = ChangesBuilder.change("status", event.getStatus().name(), EventStatus.FINISHED.name());
                    event.setStatus(EventStatus.FINISHED);
                    EventResponse eventResponse = EventMapper.mapToEventResponse(event);
                    var kafkaMessage = EventMapper.toKafkaMessageUpdated(
                            eventResponse,
                            null,
                            collectSubscriberIds(event),
                            List.of(changeItem)
                    );
                    eventSender.sendEvent(kafkaMessage);
                    log.info("Мероприятие id={} переведено в статус FINISHED", event.getId());
                }
            }
        }

        txService.saveAllToEventDb(toFinish.stream()
                .filter(e -> e.getStatus() == EventStatus.FINISHED)
                .toList());
    }

    private static List<Long> collectSubscriberIds(EventEntity event) {
        return Stream.concat(
                        event.getUsers().stream().map(u -> u.getUserId()),
                        Stream.of(event.getOwnerId())
                )
                .distinct()
                .toList();
    }
}
