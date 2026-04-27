package ru.makan1.eventmanager.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.makan1.eventmanager.event.enums.EventStatus;
import ru.makan1.eventmanager.location.entity.LocationEntity;
import ru.makan1.eventmanager.users.entity.UsersEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "event_date")
    private LocalDateTime date;

    @Column(name = "event_duration")
    @Min(30)
    private int duration;

    @Column(name = "event_cost")
    @DecimalMin("0")
    private BigDecimal cost;

    @Column(name = "event_max_places")
    @Min(1)
    private int maxPlaces;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    @Column(name = "event_name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status")
    private EventStatus status;

    @ManyToMany(mappedBy = "events")
    private Set<UsersEntity> users = new HashSet<>();

    @Column(name = "event_owner_id")
    private Long ownerId;

    @Column(name = "event_occupied_places")
    private int occupiedPlaces;

    public void addUser(UsersEntity user) {
        users.add(user);
        user.getEvents().add(this);
    }

    public void removeUser(UsersEntity user) {
        users.remove(user);
        user.getEvents().remove(this);
    }
}
