package ru.makan1.eventmanager.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.makan1.eventmanager.users.entity.UsersEntity;
import ru.makan1.eventmanager.users.repository.UsersRepository;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private final UsersRepository usersRepository;

    public UserDetailServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsersEntity usersEntity = usersRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return User.builder()
                .username(usersEntity.getLogin())
                .password(usersEntity.getPasswordHash())
                .roles(usersEntity.getRole().name())
                .build();
    }
}
