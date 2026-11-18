package com.thales_mileto.notificaciones.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Component
public class LoginValidationFilter extends OncePerRequestFilter {

    @Value("${servicio.login.url}")
    private String loginServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        if (
                authHeader == null ||
                        !authHeader.startsWith("Bearer ")
        ) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType(
                    "text/plain;charset=UTF-8"
            );

            response.getWriter().write(
                    "Token no enviado"
            );

            return;
        }

        /*
         * SOLO LA VALIDACIÓN CONTRA LOGIN
         * VA DENTRO DEL TRY
         */
        try {

            HttpHeaders headers =
                    new HttpHeaders();

            headers.set(
                    "Authorization",
                    authHeader
            );

            HttpEntity<Void> entity =
                    new HttpEntity<>(headers);

            ResponseEntity<Map> loginResponse =
                    restTemplate.exchange(
                            loginServiceUrl + "/api/auth/validar",
                            HttpMethod.GET,
                            entity,
                            Map.class
                    );

            Map body =
                    loginResponse.getBody();

            if (
                    body == null ||
                            !Boolean.TRUE.equals(
                                    body.get("valido")
                            )
            ) {

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                response.setContentType(
                        "text/plain;charset=UTF-8"
                );

                response.getWriter().write(
                        "Token inválido"
                );

                return;
            }

            String username =
                    String.valueOf(
                            body.get("username")
                    );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.emptyList()
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

        } catch (Exception e) {

            SecurityContextHolder.clearContext();

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType(
                    "text/plain;charset=UTF-8"
            );

            response.getWriter().write(
                    "No se pudo validar el token con login: "
                            + e.getMessage()
            );

            return;
        }

        /*
         * IMPORTANTE:
         * YA ESTÁ FUERA DEL TRY DE LOGIN.
         *
         * Si MongoDB, Controller o Service fallan,
         * veremos el error REAL.
         */
        filterChain.doFilter(
                request,
                response
        );
    }
}