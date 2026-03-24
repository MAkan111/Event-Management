package ru.makan1.eventmanagement.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.makan1.eventmanagement.security.exceptions.CustomAccessDeniedHandler;
import ru.makan1.eventmanagement.security.exceptions.CustomAuthenticationEntryPoint;
import ru.makan1.eventmanagement.users.entity.UsersEntity;
import ru.makan1.eventmanagement.users.enums.UserRole;
import ru.makan1.eventmanagement.users.repository.UsersRepository;

import java.util.List;

@Configuration
@EnableWebSecurity
public class GlobalSecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/event-manager-openapi.yaml").permitAll()

                        .requestMatchers("/users/auth", "/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/locations/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/locations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/locations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/locations/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/events/registrations/**").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/events/my").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/events/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/events/search").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/events/registrations/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/events").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/events/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/events/registrations/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/events/**").hasAnyRole("USER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner createAdmin(UsersRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByLogin("admin").isEmpty() && repo.findByLogin("user").isEmpty()) {

                UsersEntity admin = new UsersEntity();
                admin.setLogin("admin");
                admin.setPasswordHash(encoder.encode("password"));
                admin.setAge(30);
                admin.setRole(UserRole.ADMIN);

                UsersEntity user = new UsersEntity();
                user.setLogin("user");
                user.setPasswordHash(encoder.encode("test123"));
                user.setAge(22);
                user.setRole(UserRole.USER);

                repo.saveAll(List.of(admin, user));
            }
        };
    }
}
