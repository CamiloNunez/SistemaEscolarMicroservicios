package com.thales_mileto.academico.service;

import com.thales_mileto.academico.client.LoginClient;
import com.thales_mileto.academico.model.Docente;
import com.thales_mileto.academico.repository.DocenteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final LoginClient loginClient;

    public DocenteService(DocenteRepository docenteRepository, LoginClient loginClient) {
        this.docenteRepository = docenteRepository;
        this.loginClient = loginClient;
    }

    public List<Docente> listarTodos() {
        return docenteRepository.findAll();
    }

    public Optional<Docente> buscarPorId(String id) {
        return docenteRepository.findById(id);
    }

    public Optional<Docente> buscarPorUsuarioId(String usuarioId) {
        return docenteRepository.findByUsuarioId(usuarioId);
    }

    public Optional<Docente> buscarPorCodigo(String codigoDocente) {
        return docenteRepository.findByCodigoDocente(codigoDocente);
    }

    public List<Docente> buscarPorEstado(String estado) {
        return docenteRepository.findByEstado(estado);
    }

    public List<Docente> buscarPorEspecialidad(String especialidad) {
        return docenteRepository.findByEspecialidad(especialidad);
    }

    public Docente crear(Docente docente) {

        validarDatos(docente);

        if (docenteRepository.existsByUsuarioId(
                docente.getUsuarioId())) {

            throw new IllegalArgumentException(
                    "Ya existe un docente asociado a este usuario"
            );
        }

        if (docenteRepository.existsByCodigoDocente(
                docente.getCodigoDocente())) {

            throw new IllegalArgumentException(
                    "El código de docente ya se encuentra registrado"
            );
        }

        validarUsuarioProfesor(docente.getUsuarioId());

        docente.setId(null);

        if (docente.getEstado() == null ||
                docente.getEstado().isBlank()) {
            docente.setEstado("ACTIVO");
        }

        LocalDateTime ahora = LocalDateTime.now();

        docente.setFechaRegistro(ahora);
        docente.setFechaActualizacion(ahora);

        Docente guardado = docenteRepository.save(docente);

        // Si login rechaza la vinculación se revierte el alta para no dejar
        // un docente sin usuario correspondiente.
        try {
            loginClient.vincular(guardado.getUsuarioId(), guardado.getId(), guardado.getCodigoDocente());
        } catch (RuntimeException e) {
            docenteRepository.deleteById(guardado.getId());
            throw e;
        }

        return guardado;
    }

    public Docente actualizar(
            String id,
            Docente datos) {

        validarDatos(datos);

        Docente actual = docenteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Docente no encontrado"
                        )
                );

        Optional<Docente> porUsuario =
                docenteRepository.findByUsuarioId(
                        datos.getUsuarioId()
                );

        if (porUsuario.isPresent()
                && !porUsuario.get().getId().equals(id)) {

            throw new IllegalArgumentException(
                    "El usuario ya está asociado a otro docente"
            );
        }

        Optional<Docente> porCodigo =
                docenteRepository.findByCodigoDocente(
                        datos.getCodigoDocente()
                );

        if (porCodigo.isPresent()
                && !porCodigo.get().getId().equals(id)) {

            throw new IllegalArgumentException(
                    "El código pertenece a otro docente"
            );
        }

        String usuarioAnterior = actual.getUsuarioId();
        boolean cambiaUsuario = !datos.getUsuarioId().equals(usuarioAnterior);

        if (cambiaUsuario) {
            validarUsuarioProfesor(datos.getUsuarioId());
            // Login no permite dos usuarios sobre el mismo docente: se suelta el
            // anterior y, si el nuevo no se puede vincular, se restaura.
            boolean anteriorVinculado = usuarioAnterior != null && loginClient.obtenerPerfil(usuarioAnterior)
                    .filter(p -> id.equals(p.get("entidadId")))
                    .isPresent();
            if (anteriorVinculado) {
                loginClient.desvincular(usuarioAnterior);
            }
            try {
                loginClient.vincular(datos.getUsuarioId(), actual.getId(), datos.getCodigoDocente());
            } catch (RuntimeException e) {
                if (anteriorVinculado) {
                    loginClient.vincular(usuarioAnterior, actual.getId(), actual.getCodigoDocente());
                }
                throw e;
            }
        }

        actual.setUsuarioId(datos.getUsuarioId());
        actual.setCodigoDocente(datos.getCodigoDocente());
        actual.setEspecialidad(datos.getEspecialidad());
        actual.setEstado(datos.getEstado());
        actual.setFechaActualizacion(LocalDateTime.now());

        return docenteRepository.save(actual);
    }

    public Docente cambiarEstado(
            String id,
            String estado) {

        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Docente no encontrado"
                        )
                );

        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException(
                    "El estado es obligatorio"
            );
        }

        docente.setEstado(estado);
        docente.setFechaActualizacion(LocalDateTime.now());

        return docenteRepository.save(docente);
    }

    public void eliminar(String id) {

        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Docente no encontrado"
                        )
                );

        // Solo se suelta el perfil si realmente apunta a este docente.
        loginClient.obtenerPerfil(docente.getUsuarioId())
                .filter(p -> id.equals(p.get("entidadId")))
                .ifPresent(p -> loginClient.desvincular(docente.getUsuarioId()));

        docenteRepository.deleteById(id);
    }

    /**
     * Revisa cada docente contra login y devuelve los que tienen la vinculación rota:
     * usuario inexistente, usuario que no es PROFESOR, o perfil que apunta a otra entidad.
     */
    public List<Map<String, Object>> verificarVinculos() {
        List<Map<String, Object>> problemas = new ArrayList<>();

        for (Docente docente : docenteRepository.findAll()) {
            String problema = diagnosticar(docente);
            if (problema != null) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("docenteId", docente.getId());
                fila.put("codigoDocente", docente.getCodigoDocente());
                fila.put("usuarioId", docente.getUsuarioId());
                fila.put("problema", problema);
                problemas.add(fila);
            }
        }
        return problemas;
    }

    /**
     * Crea en login el perfil de un docente cuyo usuario es válido pero no quedó vinculado
     * (por ejemplo, docentes registrados antes de esta validación).
     */
    public Docente sincronizarVinculo(String id) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado"));
        validarUsuarioProfesor(docente.getUsuarioId());
        loginClient.vincular(docente.getUsuarioId(), docente.getId(), docente.getCodigoDocente());
        return docente;
    }

    private String diagnosticar(Docente docente) {
        if (docente.getUsuarioId() == null || docente.getUsuarioId().isBlank()) {
            return "Docente sin usuarioId";
        }
        Optional<Map<String, Object>> usuario = loginClient.obtenerUsuario(docente.getUsuarioId());
        if (usuario.isEmpty()) {
            return "El usuario no existe en login";
        }
        Object tipo = usuario.get().get("tipoUsuario");
        if (!LoginClient.TIPO_PROFESOR.equals(tipo)) {
            return "El usuario es de tipo " + tipo + ", no PROFESOR";
        }
        Object perfil = usuario.get().get("perfil");
        if (!(perfil instanceof Map<?, ?> p)) {
            return "El usuario no tiene perfil vinculado en login";
        }
        if (!docente.getId().equals(p.get("entidadId"))) {
            return "El perfil del usuario apunta a otra entidad: " + p.get("entidadId");
        }
        return null;
    }

    private void validarUsuarioProfesor(String usuarioId) {
        Map<String, Object> usuario = loginClient.obtenerUsuario(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario " + usuarioId + " no existe en login"));

        Object tipo = usuario.get("tipoUsuario");
        if (!LoginClient.TIPO_PROFESOR.equals(tipo)) {
            throw new IllegalArgumentException(
                    "El usuario " + usuario.get("username") + " es de tipo " + tipo
                            + "; solo un usuario PROFESOR puede vincularse a un docente");
        }
        if (!Boolean.TRUE.equals(usuario.get("activo"))) {
            throw new IllegalArgumentException(
                    "El usuario " + usuario.get("username") + " está inactivo");
        }
    }

    private void validarDatos(Docente docente) {

        if (docente == null) {
            throw new IllegalArgumentException(
                    "El docente es obligatorio"
            );
        }

        if (docente.getUsuarioId() == null ||
                docente.getUsuarioId().isBlank()) {
            throw new IllegalArgumentException(
                    "El usuarioId es obligatorio"
            );
        }

        if (docente.getCodigoDocente() == null ||
                docente.getCodigoDocente().isBlank()) {
            throw new IllegalArgumentException(
                    "El código de docente es obligatorio"
            );
        }

        if (docente.getEspecialidad() == null ||
                docente.getEspecialidad().isBlank()) {
            throw new IllegalArgumentException(
                    "La especialidad es obligatoria"
            );
        }
    }
}
