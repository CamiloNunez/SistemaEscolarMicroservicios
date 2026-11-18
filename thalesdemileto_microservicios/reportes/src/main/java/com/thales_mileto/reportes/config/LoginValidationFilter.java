package com.thales_mileto.reportes.config;

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
                || path.startsWith("/api/reportes/public")
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

        // =====================================================
        // VALIDAR QUE EXISTA TOKEN
        // =====================================================

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

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

        try {

            // =================================================
            // ENVIAR TOKEN AL MICROSERVICIO LOGIN
            // =================================================

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
                            loginServiceUrl
                                    + "/api/auth/validar",
                            HttpMethod.GET,
                            entity,
                            Map.class
                    );

            Map body =
                    loginResponse.getBody();

            // =================================================
            // VALIDAR RESPUESTA DE LOGIN
            // =================================================

            if (body == null ||
                    !Boolean.TRUE.equals(
                            body.get("valido")
                    )) {

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

            // =================================================
            // OBTENER USUARIO
            // =================================================

            String username =
                    String.valueOf(
                            body.get("username")
                    );

            // =================================================
            // CREAR AUTENTICACIÓN EN SPRING SECURITY
            // =================================================

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.emptyList()
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            // =================================================
            // CONTINUAR REQUEST
            // =================================================

            filterChain.doFilter(
                    request,
                    response
            );

        } catch (Exception e) {

            e.printStackTrace();

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
        }
    }
}