package ru.makan1.eventmanagement.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.makan1.eventmanagement.location.entity.LocationEntity;

@Repository
public interface LocationsRepository extends JpaRepository<LocationEntity, Long> {
}
