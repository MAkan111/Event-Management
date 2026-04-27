package ru.makan1.eventmanager.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.makan1.eventmanager.event.dto.EventResponse;
import ru.makan1.eventmanager.event.entity.EventEntity;
import ru.makan1.eventmanager.event.enums.EventStatus;
import ru.makan1.eventmanager.event.mapper.EventMapper;
import ru.makan1.eventmanager.event.repository.EventRepository;
import ru.makan1.eventmanager.utils.ChangesBuilder;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final EventRepository eventRepository;
    private final EventSender eventSender;

    @Scheduled(fixedRateString = "${spring.scheduler.fixed-rate}")
    @Transactional
    public void updateEventStatuses() {
        List<EventEntity> toStart = eventRepository.findWaitStartToStartedCandidates(EventStatus.WAIT_START);

        for (EventEntity event : toStart) {
            event.setStatus(EventStatus.STARTED);
            log.info("Мероприятие id={} переведено в статус STARTED", event.getId());
        }

        eventRepository.saveAll(toStart.stream()
                .filter(e -> e.getStatus() == EventStatus.STARTED)
                .toList());

        for (EventEntity event : toStart) {
            if (event.getStatus() != EventStatus.STARTED) {
                continue;
            }
            var changeItem = ChangesBuilder.change("status", EventStatus.WAIT_START.name(), EventStatus.STARTED.name());
            EventResponse eventResponse = EventMapper.mapToEventResponse(event);
            var kafkaMessage = EventMapper.toKafkaMessageUpdated(
                    eventResponse,
                    null,
                    collectSubscriberIds(event),
                    List.of(changeItem)
            );
            eventSender.sendEvent(kafkaMessage);
        }

        List<EventEntity> toFinish = eventRepository.findStartedToFinishCandidates();
        for (EventEntity event : toFinish) {
            event.setStatus(EventStatus.FINISHED);
            log.info("Мероприятие id={} переведено в статус FINISHED", event.getId());
        }

        eventRepository.saveAll(toFinish.stream()
                .filter(e -> e.getStatus() == EventStatus.FINISHED)
                .toList());

        for (EventEntity event : toFinish) {
            if (event.getStatus() != EventStatus.FINISHED) {
                continue;
            }
            var changeItem = ChangesBuilder.change("status", EventStatus.STARTED.name(), EventStatus.FINISHED.name());
            EventResponse eventResponse = EventMapper.mapToEventResponse(event);
            var kafkaMessage = EventMapper.toKafkaMessageUpdated(
                    eventResponse,
                    null,
                    collectSubscriberIds(event),
                    List.of(changeItem)
            );
            eventSender.sendEvent(kafkaMessage);
        }
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
