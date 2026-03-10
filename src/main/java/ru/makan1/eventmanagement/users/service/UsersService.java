package ru.makan1.eventmanagement.users.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.makan1.eventmanagement.users.dto.UsersRegistration;
import ru.makan1.eventmanagement.users.dto.UsersResponse;
import ru.makan1.eventmanagement.users.entity.UsersEntity;
import ru.makan1.eventmanagement.users.mapper.UsersMapper;
import ru.makan1.eventmanagement.users.repository.UsersRepository;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public UsersService(UsersRepository usersRepository,
                        PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsersResponse createUser(UsersRegistration usersRegistration) {
        if (usersRepository.existsByLogin(usersRegistration.login())) {
            throw new UserAlreadyExists("Пользователь с таким логином уже существует");
        }
        String encodedPassword = passwordEncoder.encode(usersRegistration.password());
        UsersEntity usersEntity = UsersMapper.mapToUsersEntity(usersRegistration);
        usersEntity.setPasswordHash(encodedPassword);
        return UsersMapper.mapToUserResponse(usersRepository.save(usersEntity));
    }

    public UsersResponse getById(Long id) {
        return usersRepository.findById(id)
                .map(UsersMapper::mapToUserResponse)
                .orElse(null);
    }
}
