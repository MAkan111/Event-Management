package ru.makan1.eventmanager.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.makan1.eventmanager.event.entity.EventEntity;
import ru.makan1.eventmanager.users.enums.UserRole;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UsersEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_login", unique = true)
    private String login;

    @Column(name = "user_password_hash")
    private String passwordHash;

    @Column(name = "user_age")
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private UserRole role;

    @ManyToMany
    @JoinTable(
            name = "event_users",
            joinColumns = @JoinColumn(
                    name = "user_id",
                    referencedColumnName = "user_id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "event_id",
                    referencedColumnName = "event_id"
            )
    )
    private List<EventEntity> events = new ArrayList<>();

    public void addEvent(EventEntity event) {
        events.add(event);
        event.getUsers().add(this);
    }

    public void removeEvent(EventEntity event) {
        events.remove(event);
        event.getUsers().remove(this);
    }
}
