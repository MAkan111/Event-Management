package ru.makan1.eventmanager.users.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.makan1.eventmanager.security.JwtUtil;
import ru.makan1.eventmanager.users.dto.JwtResponse;
import ru.makan1.eventmanager.users.dto.UsersCredentials;
import ru.makan1.eventmanager.users.dto.UsersRegistration;
import ru.makan1.eventmanager.users.dto.UsersResponse;
import ru.makan1.eventmanager.users.repository.UsersRepository;
import ru.makan1.eventmanager.users.service.UsersService;


@RestController
@RequestMapping("/users")
public class UsersController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UsersService usersService;
    private final UsersRepository usersRepository;

    public UsersController(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UserDetailsService userDetailsService,
                           UsersService usersService,
                           UsersRepository usersRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.usersService = usersService;
        this.usersRepository = usersRepository;
    }

    @PostMapping()
    public ResponseEntity<UsersResponse> createUser(@Valid @RequestBody UsersRegistration usersRegistration) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usersService.createUser(usersRegistration));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsersResponse> getUserById(@PathVariable("id") Long id) {
        UsersResponse usersResponse = usersService.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сущность с таким id не найдена"));
        return usersResponse == null
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).build()
                : ResponseEntity.ok(usersResponse);
    }

    @PostMapping("/auth")
    public ResponseEntity<JwtResponse> authUser(@RequestBody UsersCredentials usersCredentials) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usersCredentials.login(), usersCredentials.password())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(usersCredentials.login());
            Long userId = usersRepository.findByLogin(usersCredentials.login())
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"))
                    .getUserId();
            String token = jwtUtil.generateToken(userDetails, userId);

            return ResponseEntity.ok(new JwtResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
