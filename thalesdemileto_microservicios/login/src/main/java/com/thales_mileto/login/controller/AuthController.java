package com.thales_mileto.login.controller;
import com.thales_mileto.login.dto.LoginResponse;
import com.thales_mileto.login.dto.UsuarioDTO;
import com.thales_mileto.login.dto.UsuarioResponse;
import com.thales_mileto.login.model.LoginRequest;
import com.thales_mileto.login.model.TipoUsuario;
import com.thales_mileto.login.model.Usuario;
import com.thales_mileto.login.service.AuthService;
import com.thales_mileto.login.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        try {
            Usuario usuario = usuarioService.crearUsuario(usuarioDTO);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario creado exitosamente");
            response.put("id", usuario.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponse>> obtenerUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodosLosUsuarios());
    }

    /** Usuarios activos de un tipo que todavía no están vinculados (ej. ?tipoUsuario=PROFESOR). */
    @GetMapping("/usuarios/disponibles")
    public ResponseEntity<List<UsuarioResponse>> obtenerUsuariosDisponibles(
            @RequestParam TipoUsuario tipoUsuario) {
        return ResponseEntity.ok(usuarioService.obtenerUsuariosDisponibles(tipoUsuario));
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(id));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable String id,
            @Valid @RequestBody UsuarioDTO usuarioDTO) {
        try {
            return ResponseEntity.ok(usuarioService.actualizarUsuario(id, usuarioDTO));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable String id) {
        try {
            usuarioService.eliminarUsuario(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            HttpStatus status = "Usuario no encontrado".equals(e.getMessage())
                    ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
            return ResponseEntity.status(status).body(error);
        }
    }

    @GetMapping("/validar")
    public ResponseEntity<?> validarToken(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            boolean esValido = authService.validarToken(jwt);

            Map<String, Object> response = new HashMap<>();
            response.put("valido", esValido);

            if (esValido) {
                String username = authService.getUsernameFromToken(jwt);
                response.put("username", username);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Token inválido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}