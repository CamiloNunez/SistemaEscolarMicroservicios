package com.thales_mileto.login.service;

import com.thales_mileto.login.dto.PerfilUsuarioRequest;
import com.thales_mileto.login.dto.PerfilUsuarioResponse;
import com.thales_mileto.login.dto.UsuarioAutenticadoResponse;
import com.thales_mileto.login.model.PerfilUsuario;
import com.thales_mileto.login.model.TipoUsuario;
import com.thales_mileto.login.model.Usuario;
import com.thales_mileto.login.repository.PerfilUsuarioRepository;
import com.thales_mileto.login.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PerfilUsuarioService {

    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final UsuarioRepository usuarioRepository;

    public static final Map<TipoUsuario, String> ORIGEN_POR_TIPO = Map.of(
            TipoUsuario.PROFESOR, "academico.docentes",
            TipoUsuario.PADRE, "matricula.apoderados",
            TipoUsuario.ESTUDIANTE, "matricula.estudiantes"
    );

    public PerfilUsuarioResponse crear(PerfilUsuarioRequest request) {
        Usuario usuario = obtenerUsuario(request.getUsuarioId());
        TipoUsuario tipo = request.getTipoUsuario();
        String entidadId = request.getEntidadId().trim();

        validarTipo(usuario, tipo);
        validarUsuarioActivo(usuario);
        String origen = resolverOrigen(tipo, request.getOrigen());

        PerfilUsuario existente = perfilUsuarioRepository.findByUsuarioId(usuario.getId()).orElse(null);
        if (existente != null) {
            // Idempotente: los microservicios pueden reintentar la vinculación sin error.
            if (existente.getTipoUsuario() == tipo && entidadId.equals(existente.getEntidadId())) {
                return toResponse(existente);
            }
            throw new RuntimeException("El usuario ya está vinculado a " + describir(existente)
                    + ". Desvincúlelo antes de asociarlo a otra entidad");
        }

        validarEntidadLibre(tipo, entidadId, usuario.getId());

        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setUsuarioId(usuario.getId());
        perfil.setTipoUsuario(tipo);
        perfil.setEntidadId(entidadId);
        perfil.setCodigoPerfil(request.getCodigoPerfil());
        perfil.setOrigen(origen);
        perfil.setActivo(request.getActivo() == null ? true : request.getActivo());
        perfil.setFechaCreacion(LocalDateTime.now());
        perfil.setFechaActualizacion(LocalDateTime.now());

        return toResponse(perfilUsuarioRepository.save(perfil));
    }

    public PerfilUsuarioResponse actualizarPorUsuario(String usuarioId, PerfilUsuarioRequest request) {
        Usuario usuario = obtenerUsuario(usuarioId);
        PerfilUsuario perfil = perfilUsuarioRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Perfil de usuario no encontrado"));

        TipoUsuario tipo = request.getTipoUsuario() != null ? request.getTipoUsuario() : perfil.getTipoUsuario();
        validarTipo(usuario, tipo);

        String entidadId = request.getEntidadId() != null && !request.getEntidadId().isBlank()
                ? request.getEntidadId().trim()
                : perfil.getEntidadId();
        validarEntidadLibre(tipo, entidadId, usuarioId);

        perfil.setTipoUsuario(tipo);
        perfil.setEntidadId(entidadId);
        perfil.setCodigoPerfil(request.getCodigoPerfil());
        perfil.setOrigen(resolverOrigen(tipo, request.getOrigen()));
        if (request.getActivo() != null) {
            perfil.setActivo(request.getActivo());
        }
        perfil.setFechaActualizacion(LocalDateTime.now());

        return toResponse(perfilUsuarioRepository.save(perfil));
    }

    public PerfilUsuarioResponse obtenerPorUsuario(String usuarioId) {
        return perfilUsuarioRepository.findByUsuarioId(usuarioId)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Perfil de usuario no encontrado"));
    }

    public PerfilUsuarioResponse obtenerPorEntidad(String entidadId) {
        return perfilUsuarioRepository.findByEntidadId(entidadId)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Perfil asociado a la entidad no encontrado"));
    }

    public List<PerfilUsuarioResponse> listar(TipoUsuario tipoUsuario) {
        List<PerfilUsuario> perfiles = tipoUsuario == null
                ? perfilUsuarioRepository.findAll()
                : perfilUsuarioRepository.findByTipoUsuario(tipoUsuario);
        return perfiles.stream().map(this::toResponse).toList();
    }

    public void eliminarPorUsuario(String usuarioId) {
        PerfilUsuario perfil = perfilUsuarioRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Perfil de usuario no encontrado"));
        perfilUsuarioRepository.delete(perfil);
    }

    public UsuarioAutenticadoResponse obtenerContextoAutenticado(String usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        PerfilUsuarioResponse perfil = perfilUsuarioRepository.findByUsuarioId(usuario.getId())
                .map(this::toResponse)
                .orElse(null);

        return UsuarioAutenticadoResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .tipoUsuario(usuario.getTipoUsuario())
                .roles(usuario.getRoles() == null ? List.of() : usuario.getRoles().stream()
                        .map(r -> r.getNombre())
                        .toList())
                .grado(usuario.getGrado())
                .seccion(usuario.getSeccion())
                .perfil(perfil)
                .build();
    }

    public PerfilUsuarioResponse obtenerPerfilAutenticado(String usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
        return obtenerPorUsuario(usuario.getId());
    }

    public PerfilUsuarioResponse buscarPorUsuarioONull(String usuarioId) {
        return perfilUsuarioRepository.findByUsuarioId(usuarioId)
                .map(this::toResponse)
                .orElse(null);
    }

    private Usuario obtenerUsuario(String usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuarioId));
    }

    private void validarTipo(Usuario usuario, TipoUsuario tipoPerfil) {
        if (tipoPerfil == null) {
            throw new RuntimeException("El tipo de perfil es obligatorio");
        }
        if (usuario.getTipoUsuario() == null) {
            throw new RuntimeException("El usuario " + usuario.getUsername()
                    + " no tiene tipoUsuario asignado; asígnele " + tipoPerfil + " antes de vincularlo");
        }
        if (usuario.getTipoUsuario() != tipoPerfil) {
            throw new RuntimeException(
                    "El tipo del perfil (" + tipoPerfil + ") no coincide con el tipo del usuario (" + usuario.getTipoUsuario() + ")"
            );
        }
    }

    private void validarUsuarioActivo(Usuario usuario) {
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new RuntimeException("El usuario " + usuario.getUsername() + " está inactivo");
        }
    }

    /** Una misma entidad de dominio (docente, apoderado...) solo puede pertenecer a un usuario. */
    private void validarEntidadLibre(TipoUsuario tipo, String entidadId, String usuarioId) {
        boolean ocupada = perfilUsuarioRepository.findAllByTipoUsuarioAndEntidadId(tipo, entidadId).stream()
                .anyMatch(p -> !p.getUsuarioId().equals(usuarioId));
        if (ocupada) {
            throw new RuntimeException("La entidad " + entidadId + " (" + tipo + ") ya está vinculada a otro usuario");
        }
    }

    /** El origen se deriva del tipo para que no pueda apuntar a una colección incorrecta. */
    private String resolverOrigen(TipoUsuario tipo, String origenSolicitado) {
        String esperado = ORIGEN_POR_TIPO.get(tipo);
        if (esperado == null) {
            return origenSolicitado;
        }
        if (origenSolicitado != null && !origenSolicitado.isBlank() && !esperado.equals(origenSolicitado.trim())) {
            throw new RuntimeException("El origen para " + tipo + " debe ser " + esperado);
        }
        return esperado;
    }

    private String describir(PerfilUsuario perfil) {
        return (perfil.getOrigen() == null ? perfil.getTipoUsuario() : perfil.getOrigen())
                + " " + perfil.getEntidadId();
    }

    private PerfilUsuarioResponse toResponse(PerfilUsuario perfil) {
        return PerfilUsuarioResponse.builder()
                .id(perfil.getId())
                .usuarioId(perfil.getUsuarioId())
                .tipoUsuario(perfil.getTipoUsuario())
                .entidadId(perfil.getEntidadId())
                .codigoPerfil(perfil.getCodigoPerfil())
                .origen(perfil.getOrigen())
                .activo(perfil.getActivo())
                .build();
    }
}
