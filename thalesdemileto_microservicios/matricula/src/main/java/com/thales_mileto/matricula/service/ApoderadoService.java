package com.thales_mileto.matricula.service;

import com.thales_mileto.matricula.client.LoginClient;
import com.thales_mileto.matricula.model.Apoderado;
import com.thales_mileto.matricula.repository.ApoderadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ApoderadoService {

    private final ApoderadoRepository apoderadoRepository;
    private final LoginClient loginClient;

    public List<Apoderado> listar() {
        return apoderadoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Apoderado> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new RuntimeException("Debe indicar un nombre para la búsqueda");
        }
        // Minúsculas, sin tildes y con espacios simples, igual que se normaliza la columna en la consulta.
        String normalizado = Normalizer.normalize(nombre.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
        return apoderadoRepository.buscarPorNombre(normalizado);
    }

    public Apoderado obtenerPorId(String id) {
        return apoderadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apoderado no encontrado"));
    }

    public Apoderado obtenerPorUsuarioId(String usuarioId) {
        return apoderadoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("No hay un apoderado vinculado a ese usuario"));
    }

    /** Apoderados que aún no tienen usuario (por ejemplo, los importados en una migración). */
    public List<Apoderado> listarSinUsuario() {
        return apoderadoRepository.findByUsuarioIdIsNull();
    }

    public Apoderado crear(Apoderado apoderado, Principal principal) {
        apoderado.setId(null);

        if (apoderado.getDni() != null && apoderadoRepository.existsByDni(apoderado.getDni())) {
            throw new RuntimeException("Ya existe un apoderado con ese DNI");
        }

        String usuarioId = vacioANull(apoderado.getUsuarioId());
        apoderado.setUsuarioId(usuarioId);
        if (usuarioId != null) {
            validarUsuarioLibre(usuarioId, null);
            validarUsuarioPadre(usuarioId, apoderado);
        }

        if (apoderado.getReferenciaMigracion() != null
                && apoderado.getReferenciaMigracion().getFechaMigracion() == null) {
            apoderado.getReferenciaMigracion().setFechaMigracion(LocalDateTime.now());
        }

        apoderado.setEstado("ACTIVO");
        apoderado.setUsuarioRegistro(principal.getName());
        apoderado.setFechaRegistro(LocalDateTime.now());
        apoderado.setFechaActualizacion(LocalDateTime.now());

        // saveAndFlush: si viola un UNIQUE (dni, usuario_id) debe fallar ANTES de vincular en login.
        Apoderado guardado = apoderadoRepository.saveAndFlush(apoderado);

        if (usuarioId != null) {
            try {
                loginClient.vincular(usuarioId, guardado.getId(), guardado.getDni());
            } catch (RuntimeException e) {
                apoderadoRepository.delete(guardado);
                throw e;
            }
        }
        return guardado;
    }

    /** Actualiza datos del apoderado. La vinculación con el usuario se gestiona aparte. */
    public Apoderado actualizar(String id, Apoderado datos) {
        Apoderado apoderado = obtenerPorId(id);

        if (datos.getDni() != null) {
            apoderadoRepository.findByDni(datos.getDni())
                    .filter(otro -> !otro.getId().equals(id))
                    .ifPresent(otro -> {
                        throw new RuntimeException("Ya existe otro apoderado con ese DNI");
                    });
        }

        apoderado.setNombres(datos.getNombres());
        apoderado.setApellidoPaterno(datos.getApellidoPaterno());
        apoderado.setApellidoMaterno(datos.getApellidoMaterno());
        apoderado.setDni(datos.getDni());
        apoderado.setTipoRelacion(datos.getTipoRelacion());
        apoderado.setEsApoderadoPrincipal(datos.getEsApoderadoPrincipal());
        apoderado.setTelefonoCelular(datos.getTelefonoCelular());
        apoderado.setTelefonoFijo(datos.getTelefonoFijo());
        apoderado.setCorreo(datos.getCorreo());
        apoderado.setDireccion(datos.getDireccion());
        apoderado.setOcupacion(datos.getOcupacion());
        apoderado.setCentroTrabajo(datos.getCentroTrabajo());
        apoderado.setTelefonoTrabajo(datos.getTelefonoTrabajo());
        apoderado.setEstado(datos.getEstado());
        apoderado.setFechaActualizacion(LocalDateTime.now());

        // Si está vinculado, el nuevo DNI debe seguir coincidiendo con el del usuario.
        if (apoderado.getUsuarioId() != null) {
            validarUsuarioPadre(apoderado.getUsuarioId(), apoderado);
        }

        return apoderadoRepository.save(apoderado);
    }

    public Apoderado vincularUsuario(String id, String usuarioId) {
        Apoderado apoderado = obtenerPorId(id);
        if (usuarioId.equals(apoderado.getUsuarioId())) {
            loginClient.vincular(usuarioId, apoderado.getId(), apoderado.getDni());
            return apoderado;
        }
        if (apoderado.getUsuarioId() != null) {
            throw new IllegalStateException("El apoderado ya está vinculado al usuario "
                    + apoderado.getUsuarioId() + ". Desvincúlelo primero");
        }

        validarUsuarioLibre(usuarioId, id);
        validarUsuarioPadre(usuarioId, apoderado);

        loginClient.vincular(usuarioId, apoderado.getId(), apoderado.getDni());
        apoderado.setUsuarioId(usuarioId);
        apoderado.setFechaActualizacion(LocalDateTime.now());
        try {
            // saveAndFlush: el error de BD debe ocurrir aquí (y compensarse), no en el commit.
            return apoderadoRepository.saveAndFlush(apoderado);
        } catch (RuntimeException e) {
            loginClient.desvincular(usuarioId);
            throw e;
        }
    }

    public Apoderado desvincularUsuario(String id) {
        Apoderado apoderado = obtenerPorId(id);
        if (apoderado.getUsuarioId() == null) {
            return apoderado;
        }
        desvincularEnLogin(apoderado);
        apoderado.setUsuarioId(null);
        apoderado.setFechaActualizacion(LocalDateTime.now());
        return apoderadoRepository.save(apoderado);
    }

    public void eliminar(String id) {
        Apoderado apoderado = obtenerPorId(id);
        if (apoderado.getUsuarioId() != null) {
            desvincularEnLogin(apoderado);
        }
        apoderadoRepository.delete(apoderado);
    }

    /**
     * Revisa los apoderados vinculados contra login y devuelve los inconsistentes:
     * usuario inexistente, tipo distinto de PADRE, DNI distinto o perfil apuntando a otra entidad.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // no retener una conexión de BD durante N llamadas HTTP
    public List<Map<String, Object>> verificarVinculos() {
        List<Map<String, Object>> problemas = new ArrayList<>();
        for (Apoderado apoderado : apoderadoRepository.findAll()) {
            if (apoderado.getUsuarioId() == null) {
                continue;
            }
            String problema = diagnosticar(apoderado);
            if (problema != null) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("apoderadoId", apoderado.getId());
                fila.put("dni", apoderado.getDni());
                fila.put("usuarioId", apoderado.getUsuarioId());
                fila.put("problema", problema);
                problemas.add(fila);
            }
        }
        return problemas;
    }

    private String diagnosticar(Apoderado apoderado) {
        Optional<Map<String, Object>> usuario = loginClient.obtenerUsuario(apoderado.getUsuarioId());
        if (usuario.isEmpty()) {
            return "El usuario no existe en login";
        }
        try {
            validarCoincidencia(usuario.get(), apoderado);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        Object perfil = usuario.get().get("perfil");
        if (!(perfil instanceof Map<?, ?> p)) {
            return "El usuario no tiene perfil vinculado en login";
        }
        if (!apoderado.getId().equals(p.get("entidadId"))) {
            return "El perfil del usuario apunta a otra entidad: " + p.get("entidadId");
        }
        return null;
    }

    private void desvincularEnLogin(Apoderado apoderado) {
        // Solo se elimina el perfil si apunta a este apoderado.
        loginClient.obtenerPerfil(apoderado.getUsuarioId())
                .filter(p -> apoderado.getId().equals(p.get("entidadId")))
                .ifPresent(p -> loginClient.desvincular(apoderado.getUsuarioId()));
    }

    private void validarUsuarioLibre(String usuarioId, String apoderadoIdActual) {
        apoderadoRepository.findByUsuarioId(usuarioId)
                .filter(otro -> !otro.getId().equals(apoderadoIdActual))
                .ifPresent(otro -> {
                    throw new IllegalStateException("El usuario ya está vinculado al apoderado " + otro.getId());
                });
    }

    private void validarUsuarioPadre(String usuarioId, Apoderado apoderado) {
        Map<String, Object> usuario = loginClient.obtenerUsuario(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario " + usuarioId + " no existe en login"));
        validarCoincidencia(usuario, apoderado);
        if (!Boolean.TRUE.equals(usuario.get("activo"))) {
            throw new IllegalArgumentException("El usuario " + usuario.get("username") + " está inactivo");
        }
    }

    /** El usuario debe ser PADRE y, si ambos tienen documento, cedula == dni. */
    private void validarCoincidencia(Map<String, Object> usuario, Apoderado apoderado) {
        Object tipo = usuario.get("tipoUsuario");
        if (!LoginClient.TIPO_PADRE.equals(tipo)) {
            throw new IllegalArgumentException("El usuario " + usuario.get("username") + " es de tipo " + tipo
                    + "; solo un usuario PADRE puede vincularse a un apoderado");
        }
        String cedula = vacioANull(usuario.get("cedula") == null ? null : usuario.get("cedula").toString());
        String dni = vacioANull(apoderado.getDni());
        if (cedula != null && dni != null && !cedula.equalsIgnoreCase(dni)) {
            throw new IllegalArgumentException("El documento del usuario (" + cedula
                    + ") no coincide con el DNI del apoderado (" + dni + ")");
        }
    }

    private static String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
