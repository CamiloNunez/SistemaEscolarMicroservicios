package com.thales_mileto.login.service;

import com.thales_mileto.login.dto.PerfilUsuarioResponse;
import com.thales_mileto.login.dto.UsuarioDTO;
import com.thales_mileto.login.dto.UsuarioResponse;
import com.thales_mileto.login.model.PerfilUsuario;
import com.thales_mileto.login.model.Rol;
import com.thales_mileto.login.model.TipoUsuario;
import com.thales_mileto.login.model.Usuario;
import com.thales_mileto.login.repository.PerfilUsuarioRepository;
import com.thales_mileto.login.repository.RolRepository;
import com.thales_mileto.login.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario crearUsuario(UsuarioDTO dto) {
        validarUnicidad(dto.getUsername(), dto.getEmail(), null);
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }

        Usuario usuario = new Usuario();
        aplicarDatos(usuario, dto, true);
        usuario.setFechaCreacion(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    public List<UsuarioResponse> obtenerTodosLosUsuarios() {
        Map<String, PerfilUsuario> perfiles = perfilesPorUsuario();
        return usuarioRepository.findAll().stream()
                .map(u -> toResponse(u, perfiles.get(u.getId())))
                .toList();
    }

    /**
     * Usuarios activos del tipo indicado que aún no están vinculados a ninguna entidad.
     * Pensado para los selectores al registrar un docente o un apoderado, de modo que
     * solo se ofrezcan usuarios válidos para esa vinculación.
     */
    public List<UsuarioResponse> obtenerUsuariosDisponibles(TipoUsuario tipoUsuario) {
        Map<String, PerfilUsuario> perfiles = perfilesPorUsuario();
        return usuarioRepository.findByTipoUsuario(tipoUsuario).stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .filter(u -> !perfiles.containsKey(u.getId()))
                .map(u -> toResponse(u, null))
                .toList();
    }

    public Usuario obtenerUsuarioEntidadPorId(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public UsuarioResponse obtenerUsuarioPorId(String id) {
        return toResponse(obtenerUsuarioEntidadPorId(id));
    }

    public void eliminarUsuario(String id) {
        Usuario usuario = obtenerUsuarioEntidadPorId(id);
        // Borrar un usuario vinculado dejaría al docente/apoderado apuntando a un id
        // inexistente. Primero se debe desvincular desde el microservicio dueño de la
        // entidad (o simplemente desactivar el usuario).
        perfilUsuarioRepository.findByUsuarioId(id).ifPresent(perfil -> {
            throw new RuntimeException("El usuario está vinculado a " + perfil.getOrigen() + " "
                    + perfil.getEntidadId() + ". Desvincúlelo primero o desactive el usuario");
        });
        usuarioRepository.delete(usuario);
    }

    public UsuarioResponse actualizarUsuario(String id, UsuarioDTO dto) {
        Usuario usuario = obtenerUsuarioEntidadPorId(id);

        // Al editar no consultamos por username/email si el valor no cambió.
        // Esto evita que una actualización de contraseña falle por registros
        // históricos duplicados que ya existan en MongoDB.
        validarUnicidadActualizacion(usuario, dto);

        // Un usuario vinculado no puede cambiar de tipo: el profesor dejaría de
        // coincidir con su docente (o el padre con su apoderado).
        PerfilUsuario perfil = perfilUsuarioRepository.findByUsuarioId(id).orElse(null);
        if (perfil != null && dto.getTipoUsuario() != perfil.getTipoUsuario()) {
            throw new RuntimeException("No se puede cambiar el tipo a " + dto.getTipoUsuario()
                    + ": el usuario está vinculado como " + perfil.getTipoUsuario()
                    + " (" + perfil.getOrigen() + " " + perfil.getEntidadId() + ")");
        }

        aplicarDatos(usuario, dto, false);
        return toResponse(usuarioRepository.save(usuario), perfil);
    }

    private void validarUnicidadActualizacion(Usuario actual, UsuarioDTO dto) {
        String nuevoUsername = dto.getUsername() == null ? "" : dto.getUsername().trim();
        String nuevoEmail = dto.getEmail() == null ? "" : dto.getEmail().trim();

        if (!nuevoUsername.equalsIgnoreCase(actual.getUsername())) {
            boolean ocupado = usuarioRepository.findAllByUsername(nuevoUsername).stream()
                    .anyMatch(u -> !u.getId().equals(actual.getId()));
            if (ocupado) {
                throw new RuntimeException("El username ya existe");
            }
        }

        if (!nuevoEmail.equalsIgnoreCase(actual.getEmail())) {
            boolean ocupado = usuarioRepository.findAllByEmail(nuevoEmail).stream()
                    .anyMatch(u -> !u.getId().equals(actual.getId()));
            if (ocupado) {
                throw new RuntimeException("El email ya existe");
            }
        }
    }

    private void validarUnicidad(String username, String email, String idActual) {
        boolean usernameOcupado = usuarioRepository.findAllByUsername(username).stream()
                .anyMatch(u -> idActual == null || !u.getId().equals(idActual));
        if (usernameOcupado) {
            throw new RuntimeException("El username ya existe");
        }

        boolean emailOcupado = usuarioRepository.findAllByEmail(email).stream()
                .anyMatch(u -> idActual == null || !u.getId().equals(idActual));
        if (emailOcupado) {
            throw new RuntimeException("El email ya existe");
        }
    }

    private void aplicarDatos(Usuario usuario, UsuarioDTO dto, boolean creando) {
        usuario.setUsername(dto.getUsername().trim());
        usuario.setEmail(dto.getEmail().trim());
        usuario.setNombreCompleto(dto.getNombreCompleto().trim());
        usuario.setCedula(dto.getCedula());
        usuario.setTelefono(dto.getTelefono());
        usuario.setTipoUsuario(dto.getTipoUsuario());
        usuario.setGrado(dto.getGrado());
        usuario.setSeccion(dto.getSeccion());

        if (creando) usuario.setActivo(dto.getActivo() == null ? true : dto.getActivo());
        else if (dto.getActivo() != null) usuario.setActivo(dto.getActivo());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        List<String> nombresRoles = dto.getRoles() == null ? Collections.emptyList() : dto.getRoles();
        List<Rol> roles = nombresRoles.stream()
                .map(nombreRol -> rolRepository.findAllByNombre(nombreRol).stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + nombreRol)))
                .collect(Collectors.toList());
        usuario.setRoles(roles);
    }

    private Map<String, PerfilUsuario> perfilesPorUsuario() {
        return perfilUsuarioRepository.findAll().stream()
                .collect(Collectors.toMap(PerfilUsuario::getUsuarioId, Function.identity(), (a, b) -> a));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return toResponse(u, perfilUsuarioRepository.findByUsuarioId(u.getId()).orElse(null));
    }

    private UsuarioResponse toResponse(Usuario u, PerfilUsuario perfil) {
        List<String> roles = u.getRoles() == null ? Collections.emptyList()
                : u.getRoles().stream().map(Rol::getNombre).toList();
        return new UsuarioResponse(
                u.getId(), u.getUsername(), u.getEmail(), u.getNombreCompleto(),
                u.getCedula(), u.getTelefono(), u.getActivo(), roles,
                u.getFechaCreacion(), u.getUltimoAcceso(), u.getTipoUsuario(),
                u.getGrado(), u.getSeccion(),
                perfil == null ? null : PerfilUsuarioResponse.builder()
                        .id(perfil.getId())
                        .usuarioId(perfil.getUsuarioId())
                        .tipoUsuario(perfil.getTipoUsuario())
                        .entidadId(perfil.getEntidadId())
                        .codigoPerfil(perfil.getCodigoPerfil())
                        .origen(perfil.getOrigen())
                        .activo(perfil.getActivo())
                        .build()
        );
    }
}
