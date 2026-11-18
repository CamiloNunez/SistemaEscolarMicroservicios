package com.thales_mileto.reportes.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginValidationFilter loginValidationFilter;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // =================================================
                // CORS
                // =================================================

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                // =================================================
                // CSRF
                // =================================================

                .csrf(csrf ->
                        csrf.disable()
                )

                // =================================================
                // SIN SESIONES
                // =================================================

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // =================================================
                // AUTORIZACIÓN
                // =================================================

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/reportes/public/**"
                        ).permitAll()

                        .requestMatchers(
                                "/error"
                        ).permitAll()

                        .anyRequest()
                        .authenticated()
                )

                // =================================================
                // FILTRO LOGIN
                // =================================================

                .addFilterBefore(
                        loginValidationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // =============================================================
    // CONFIGURACIÓN CORS
    // =============================================================

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        // =========================================================
        // ORÍGENES PERMITIDOS
        // =========================================================

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:3000",
                        "http://127.0.0.1:3000",
                        "http://localhost:5500",
                        "http://127.0.0.1:5500"
                )
        );

        // =========================================================
        // MÉTODOS
        // =========================================================

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        // =========================================================
        // HEADERS
        // =========================================================

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin"
                )
        );

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );

        configuration.setAllowCredentials(false);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}