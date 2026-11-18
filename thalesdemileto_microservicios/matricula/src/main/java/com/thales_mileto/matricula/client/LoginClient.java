package com.thales_mileto.matricula.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cliente del microservicio login. Reenvía el token del usuario que hizo la petición,
 * de modo que login aplica sus propias reglas de seguridad.
 */
@Component
public class LoginClient {

    public static final String TIPO_PADRE = "PADRE";
    public static final String ORIGEN_APODERADOS = "matricula.apoderados";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${servicio.login.url}")
    private String loginUrl;

    /** Usuario de login o vacío si no existe. */
    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> obtenerUsuario(String usuarioId) {
        try {
            ResponseEntity<Map> r = restTemplate.exchange(
                    loginUrl + "/api/auth/usuarios/" + usuarioId, HttpMethod.GET, entidad(null), Map.class);
            return Optional.ofNullable(r.getBody());
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return Optional.empty();
            }
            throw error("consultar el usuario " + usuarioId, e);
        } catch (ResourceAccessException e) {
            throw sinConexion();
        }
    }

    /** Crea (o confirma, si ya existe igual) la vinculación usuario -> apoderado. */
    public void vincular(String usuarioId, String apoderadoId, String codigoPerfil) {
        Map<String, Object> body = new HashMap<>();
        body.put("usuarioId", usuarioId);
        body.put("tipoUsuario", TIPO_PADRE);
        body.put("entidadId", apoderadoId);
        body.put("codigoPerfil", codigoPerfil);
        body.put("origen", ORIGEN_APODERADOS);
        try {
            restTemplate.exchange(loginUrl + "/api/auth/perfiles", HttpMethod.POST, entidad(body), Map.class);
        } catch (HttpStatusCodeException e) {
            throw error("vincular el usuario " + usuarioId, e);
        } catch (ResourceAccessException e) {
            throw sinConexion();
        }
    }

    /** Elimina la vinculación del usuario. No falla si ya no existía. */
    public void desvincular(String usuarioId) {
        try {
            restTemplate.exchange(loginUrl + "/api/auth/perfiles/usuario/" + usuarioId,
                    HttpMethod.DELETE, entidad(null), Map.class);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() != HttpStatus.NOT_FOUND.value()) {
                throw error("desvincular el usuario " + usuarioId, e);
            }
        } catch (ResourceAccessException e) {
            throw sinConexion();
        }
    }

    /** Perfil registrado en login para el usuario, o vacío si no tiene. */
    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> obtenerPerfil(String usuarioId) {
        try {
            ResponseEntity<Map> r = restTemplate.exchange(
                    loginUrl + "/api/auth/perfiles/usuario/" + usuarioId, HttpMethod.GET, entidad(null), Map.class);
            return Optional.ofNullable(r.getBody());
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return Optional.empty();
            }
            throw error("consultar el perfil de " + usuarioId, e);
        } catch (ResourceAccessException e) {
            throw sinConexion();
        }
    }

    private HttpEntity<Object> entidad(Object body) {
        HttpHeaders headers = new HttpHeaders();
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (auth != null) {
                headers.set(HttpHeaders.AUTHORIZATION, auth);
            }
        }
        return new HttpEntity<>(body, headers);
    }

    private IllegalStateException error(String accion, HttpStatusCodeException e) {
        String mensaje = e.getStatusText();
        try {
            Map<?, ?> cuerpo = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);
            if (cuerpo.get("mensaje") != null) {
                mensaje = cuerpo.get("mensaje").toString();
            }
        } catch (Exception ignored) {
            // El cuerpo no es JSON; se usa el estado HTTP.
        }
        return new IllegalStateException("Login rechazó " + accion + ": " + mensaje);
    }

    private IllegalStateException sinConexion() {
        return new IllegalStateException("No se pudo contactar al microservicio login para validar la vinculación");
    }
}
