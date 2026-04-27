package ru.makan1.eventmanager.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.makan1.eventmanager.event.entity.EventEntity;
import ru.makan1.eventmanager.event.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {

    List<EventEntity> findByOwnerId(Long ownerId);

    @Query("SELECT e FROM EventEntity e WHERE e.status = :status AND CAST(e.date as date ) <= current date ")
    List<EventEntity> findWaitStartToStartedCandidates(EventStatus status);

    @Query(
            value = """
                    SELECT *
                    FROM event e
                    WHERE e.event_status = 'STARTED'
                      AND e.event_date + (e.event_duration || ' minutes')::interval <= now()
                    """,
            nativeQuery = true
    )
    List<EventEntity> findStartedToFinishCandidates();

    @Query("""
            select e
            from EventEntity e
            left join e.location l
            where (:name is null or e.name = :name)
              and (:placesMin is null or :placesMin <= 0 or e.maxPlaces >= :placesMin)
              and (:placesMax is null or :placesMax <= 0 or e.maxPlaces <= :placesMax)
              and (:dateStartAfter is null or e.date >= :dateStartAfter)
              and (:dateStartBefore is null or e.date <= :dateStartBefore)
              and (:costMin is null or e.cost >= :costMin)
              and (:costMax is null or e.cost <= :costMax)
              and (:durationMin is null or :durationMin <= 0 or e.duration >= :durationMin)
              and (:durationMax is null or :durationMax <= 0 or e.duration <= :durationMax)
              and (:locationId is null or l.id = :locationId)
              and (:status is null or e.status = :status)
            """)
    List<EventEntity> search(
            @Param("name") String name,
            @Param("placesMin") Integer placesMin,
            @Param("placesMax") Integer placesMax,
            @Param("dateStartAfter") LocalDateTime dateStartAfter,
            @Param("dateStartBefore") LocalDateTime dateStartBefore,
            @Param("costMin") BigDecimal costMin,
            @Param("costMax") BigDecimal costMax,
            @Param("durationMin") Integer durationMin,
            @Param("durationMax") Integer durationMax,
            @Param("locationId") Long locationId,
            @Param("status") EventStatus status
    );
}
