package com.thales_mileto.login.controller;

import com.thales_mileto.login.dto.PerfilUsuarioRequest;
import com.thales_mileto.login.dto.PerfilUsuarioResponse;
import com.thales_mileto.login.dto.UsuarioAutenticadoResponse;
import com.thales_mileto.login.model.TipoUsuario;
import com.thales_mileto.login.service.PerfilUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PerfilUsuarioController {

    private final PerfilUsuarioService perfilUsuarioService;

    /**
     * Contexto completo del usuario identificado por el JWT.
     * Devuelve usuario, roles, tipoUsuario y la relación con su perfil de dominio.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        try {
            UsuarioAutenticadoResponse response =
                    perfilUsuarioService.obtenerContextoAutenticado(authentication.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** Alias pensado para consumo entre microservicios. */
    @GetMapping("/contexto")
    public ResponseEntity<?> contexto(Authentication authentication) {
        return me(authentication);
    }

    /** Devuelve solo el perfil funcional asociado al usuario autenticado. */
    @GetMapping("/me/perfil")
    public ResponseEntity<?> miPerfil(Authentication authentication) {
        try {
            return ResponseEntity.ok(
                    perfilUsuarioService.obtenerPerfilAutenticado(authentication.getName())
            );
        } catch (Exception e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** Busca la relación de perfil por id de usuario. */
    @GetMapping("/perfiles/usuario/{usuarioId}")
    public ResponseEntity<?> perfilPorUsuario(@PathVariable String usuarioId) {
        try {
            return ResponseEntity.ok(perfilUsuarioService.obtenerPorUsuario(usuarioId));
        } catch (Exception e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** Busca qué usuario corresponde a una entidad de dominio (docente/apoderado/estudiante). */
    @GetMapping("/perfiles/entidad/{entidadId}")
    public ResponseEntity<?> perfilPorEntidad(@PathVariable String entidadId) {
        try {
            return ResponseEntity.ok(perfilUsuarioService.obtenerPorEntidad(entidadId));
        } catch (Exception e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** Lista perfiles; puede filtrarse con ?tipoUsuario=PROFESOR, PADRE, ESTUDIANTE, etc. */
    @GetMapping("/perfiles")
    public ResponseEntity<List<PerfilUsuarioResponse>> listarPerfiles(
            @RequestParam(required = false) TipoUsuario tipoUsuario) {
        return ResponseEntity.ok(perfilUsuarioService.listar(tipoUsuario));
    }

    /** Crea la relación usuario -> perfil de dominio. */
    @PostMapping("/perfiles")
    public ResponseEntity<?> crearPerfil(@Valid @RequestBody PerfilUsuarioRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(perfilUsuarioService.crear(request));
        } catch (Exception e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /** Actualiza la relación de un usuario con su perfil de dominio. */
    @PutMapping("/perfiles/usuario/{usuarioId}")
    public ResponseEntity<?> actualizarPerfil(
            @PathVariable String usuarioId,
            @Valid @RequestBody PerfilUsuarioRequest request) {
        try {
            return ResponseEntity.ok(perfilUsuarioService.actualizarPorUsuario(usuarioId, request));
        } catch (Exception e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/perfiles/usuario/{usuarioId}")
    public ResponseEntity<?> eliminarPerfil(@PathVariable String usuarioId) {
        try {
            perfilUsuarioService.eliminarPorUsuario(usuarioId);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Perfil de usuario eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(error);
    }
}
