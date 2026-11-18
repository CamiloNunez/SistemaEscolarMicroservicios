package com.thales_mileto.academico.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cliente del microservicio login. Por defecto reenvía el token del usuario que hizo
 * la petición, de modo que login aplica sus propias reglas de seguridad. Los procesos
 * sin petición HTTP (como el seeder) pasan un token explícito.
 */
@Component
public class LoginClient {

    public static final String TIPO_PROFESOR = "PROFESOR";
    public static final String ORIGEN_DOCENTES = "academico.docentes";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${servicio.login.url}")
    private String loginUrl;

    public LoginClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        // Margen para el arranque en frío del servicio login en Render.
        factory.setReadTimeout(60_000);
        this.restTemplate = new RestTemplate(factory);
    }

    /** Inicia sesión y devuelve el header Authorization ("Bearer ..."). */
    @SuppressWarnings("unchecked")
    public String iniciarSesion(String username, String password) {
        try {
            ResponseEntity<Map> r = restTemplate.postForEntity(
                    loginUrl + "/api/auth/login", Map.of("username", username, "password", password), Map.class);
            Object token = r.getBody() == null ? null : r.getBody().get("token");
            if (token == null) {
                throw new IllegalStateException("Login no devolvió token para " + username);
            }
            return "Bearer " + token;
        } catch (HttpStatusCodeException e) {
            throw error("iniciar sesión como " + username, e);
        } catch (ResourceAccessException e) {
            throw sinConexion();
        }
    }

    /** Catálogo completo de usuarios de login. */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarUsuarios(String authHeader) {
        try {
            ResponseEntity<List> r = restTemplate.exchange(
                    loginUrl + "/api/auth/usuarios", HttpMethod.GET, entidad(null, authHeader), List.class);
            return r.getBody() == null ? List.of() : r.getBody();
        } catch (HttpStatusCodeException e) {
            throw error("listar usuarios", e);
        } catch (ResourceAccessException e) {
            throw sinConexion();
        }
    }

    /** Usuario de login o vacío si no existe. */
    public Optional<Map<String, Object>> obtenerUsuario(String usuarioId) {
        return obtenerUsuario(usuarioId, null);
    }

    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> obtenerUsuario(String usuarioId, String authHeader) {
        try {
            ResponseEntity<Map> r = restTemplate.exchange(
                    loginUrl + "/api/auth/usuarios/" + usuarioId, HttpMethod.GET, entidad(null, authHeader), Map.class);
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

    /** Crea (o confirma, si ya existe igual) la vinculación usuario -> docente. */
    public void vincular(String usuarioId, String docenteId, String codigoDocente) {
        vincular(usuarioId, docenteId, codigoDocente, null);
    }

    public void vincular(String usuarioId, String docenteId, String codigoDocente, String authHeader) {
        Map<String, Object> body = new HashMap<>();
        body.put("usuarioId", usuarioId);
        body.put("tipoUsuario", TIPO_PROFESOR);
        body.put("entidadId", docenteId);
        body.put("codigoPerfil", codigoDocente);
        body.put("origen", ORIGEN_DOCENTES);
        try {
            restTemplate.exchange(loginUrl + "/api/auth/perfiles", HttpMethod.POST, entidad(body, authHeader), Map.class);
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
                    HttpMethod.DELETE, entidad(null, null), Map.class);
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
                    loginUrl + "/api/auth/perfiles/usuario/" + usuarioId, HttpMethod.GET, entidad(null, null), Map.class);
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

    /** authHeader explícito; si es null se toma el de la petición HTTP en curso. */
    private HttpEntity<Object> entidad(Object body, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        String auth = authHeader;
        if (auth == null) {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            }
        }
        if (auth != null) {
            headers.set(HttpHeaders.AUTHORIZATION, auth);
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
