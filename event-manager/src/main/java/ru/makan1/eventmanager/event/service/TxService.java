package ru.makan1.eventmanager.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.makan1.eventmanager.event.entity.EventEntity;
import ru.makan1.eventmanager.event.repository.EventRepository;
import ru.makan1.eventmanager.location.entity.LocationEntity;
import ru.makan1.eventmanager.location.repository.LocationsRepository;
import ru.makan1.eventmanager.users.entity.UsersEntity;
import ru.makan1.eventmanager.users.repository.UsersRepository;

import java.util.List;

@Service
public class TxService {
    private final EventRepository eventRepository;
    private final UsersRepository usersRepository;
    private final LocationsRepository locationsRepository;

    public TxService(EventRepository eventRepository,
                     UsersRepository usersRepository,
                     LocationsRepository locationsRepository
    ) {
        this.eventRepository = eventRepository;
        this.usersRepository = usersRepository;
        this.locationsRepository = locationsRepository;
    }

    @Transactional
    public EventEntity saveToEventDb(EventEntity eventEntity) {
        return eventRepository.save(eventEntity);
    }

    @Transactional
    public UsersEntity saveToUsersDb(UsersEntity usersEntity) {
        return usersRepository.save(usersEntity);
    }

    @Transactional
    public LocationEntity saveToLocationsDb(LocationEntity locationEntity) {
        return locationsRepository.save(locationEntity);
    }

    @Transactional
    public void saveAllToEventDb(List<EventEntity> eventEntities) {
        eventRepository.saveAll(eventEntities);
    }
}
