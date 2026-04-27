package ru.makan1.eventmanager.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.makan1.eventmanager.users.entity.UsersEntity;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, Long> {
    Optional<UsersEntity> findByLogin(String username);
    boolean existsByLogin(String login);
}
