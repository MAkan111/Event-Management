package ru.makan1.eventmanagement.security.exceptions;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ru.makan1.eventmanagement.users.dto.SecurityErrorResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {
        var securityErrorResponse = new SecurityErrorResponse(
                "Сначала нужно авторизоваться",
                authException.getMessage(),
                request.getServletPath(),
                LocalDateTime.now()
        );

        var securityErrorResponseString = new ObjectMapper().writeValueAsString(securityErrorResponse);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(securityErrorResponseString);
    }
}
