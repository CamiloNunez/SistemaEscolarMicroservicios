package com.thales_mileto.login.service;

import com.thales_mileto.login.dto.LoginResponse;
import com.thales_mileto.login.model.LoginRequest;
import com.thales_mileto.login.model.Usuario;
import com.thales_mileto.login.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PerfilUsuarioService perfilUsuarioService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername() == null ? "" : loginRequest.getUsername().trim();
        String password = loginRequest.getPassword() == null ? "" : loginRequest.getPassword();

        List<Usuario> candidatos = usuarioRepository.findAllByUsername(username);
        if (candidatos.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // No usamos findByUsername porque la base ya puede contener registros históricos
        // duplicados. Elegimos únicamente entre los usuarios activos cuya contraseña coincide.
        List<Usuario> coincidencias = candidatos.stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .filter(u -> u.getPassword() != null && passwordEncoder.matches(password, u.getPassword()))
                .collect(Collectors.toCollection(ArrayList::new));

        if (coincidencias.isEmpty()) {
            boolean existeActivo = candidatos.stream().anyMatch(u -> Boolean.TRUE.equals(u.getActivo()));
            if (!existeActivo) {
                throw new RuntimeException("Usuario inactivo");
            }
            throw new RuntimeException("Credenciales inválidas");
        }

        Usuario usuario = seleccionarUsuario(coincidencias);

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String token = generarToken(usuario);

        return LoginResponse.builder()
                .token(token)
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .tipoUsuario(usuario.getTipoUsuario())
                .roles(usuario.getRoles() == null ? List.of() : usuario.getRoles().stream()
                        .map(rol -> rol.getNombre())
                        .collect(Collectors.toList()))
                .grado(usuario.getGrado())
                .seccion(usuario.getSeccion())
                .perfil(perfilUsuarioService.buscarPorUsuarioONull(usuario.getId()))
                .build();
    }

    private Usuario seleccionarUsuario(List<Usuario> coincidencias) {
        if (coincidencias.size() == 1) {
            return coincidencias.get(0);
        }

        // Si existen duplicados históricos con la misma contraseña, preferimos el que
        // ya tenga un perfil funcional vinculado. Así se conserva la relación real del sistema.
        List<Usuario> conPerfil = coincidencias.stream()
                .filter(u -> perfilUsuarioService.buscarPorUsuarioONull(u.getId()) != null)
                .toList();

        List<Usuario> candidatos = conPerfil.isEmpty() ? coincidencias : conPerfil;

        // Selección determinista para datos heredados: último acceso, fecha de creación e id.
        return candidatos.stream()
                .max(Comparator
                        .comparing(Usuario::getUltimoAcceso,
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Usuario::getFechaCreacion,
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Usuario::getId,
                                Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow(() -> new RuntimeException("No se pudo determinar el usuario"));
    }

    private String generarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", usuario.getId());
        claims.put("username", usuario.getUsername());
        claims.put("email", usuario.getEmail());
        claims.put("tipoUsuario", usuario.getTipoUsuario());
        claims.put("roles", usuario.getRoles() == null ? List.of() : usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(Collectors.toList()));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        // El subject ahora es el ID, no el username. Un ID de Mongo es único incluso
        // cuando existan usernames duplicados heredados.
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario.getId())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        Object username = claims.get("username");
        return username == null ? null : username.toString();
    }

    private Claims getClaims(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validarToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
