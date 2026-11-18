package com.thales_mileto.matricula.service;

import com.thales_mileto.matricula.model.Estudiante;
import com.thales_mileto.matricula.repository.EstudianteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
@RequiredArgsConstructor
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    public List<Estudiante> listar() {
        return estudianteRepository.findAll();
    }

    public Estudiante obtenerPorId(String id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Estudiante> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new RuntimeException("Debe indicar un nombre para la búsqueda");
        }
        // Minúsculas, sin tildes y con espacios simples, igual que se normaliza la columna en la consulta.
        String normalizado = Normalizer.normalize(nombre.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
        return estudianteRepository.buscarPorNombre(normalizado);
    }

    public Estudiante crear(Estudiante estudiante, Principal principal) {
        estudiante.setId(null);

        if (estudiante.getDni() != null && estudianteRepository.existsByDni(estudiante.getDni())) {
            throw new RuntimeException("Ya existe un estudiante con ese DNI");
        }

        if (estudiante.getCodigoEstudiante() != null &&
                estudianteRepository.existsByCodigoEstudiante(estudiante.getCodigoEstudiante())) {
            throw new RuntimeException("Ya existe un estudiante con ese código");
        }

        estudiante.setEstado("ACTIVO");
        estudiante.setUsuarioRegistro(principal.getName());
        estudiante.setFechaRegistro(LocalDateTime.now());
        estudiante.setFechaActualizacion(LocalDateTime.now());

        return estudianteRepository.save(estudiante);
    }

    public Estudiante actualizar(String id, Estudiante datos) {
        Estudiante estudiante = obtenerPorId(id);

        estudiante.setNombres(datos.getNombres());
        estudiante.setApellidoPaterno(datos.getApellidoPaterno());
        estudiante.setApellidoMaterno(datos.getApellidoMaterno());
        estudiante.setDni(datos.getDni());
        estudiante.setCodigoEstudiante(datos.getCodigoEstudiante());
        estudiante.setFechaNacimiento(datos.getFechaNacimiento());
        estudiante.setGenero(datos.getGenero());
        estudiante.setLugarNacimiento(datos.getLugarNacimiento());
        estudiante.setNacionalidad(datos.getNacionalidad());
        estudiante.setDireccion(datos.getDireccion());
        estudiante.setTelefono(datos.getTelefono());
        estudiante.setCorreo(datos.getCorreo());
        estudiante.setGrupoSanguineo(datos.getGrupoSanguineo());
        estudiante.setCondicionesEspeciales(datos.getCondicionesEspeciales());
        estudiante.setRequiereAtencionEspecial(datos.getRequiereAtencionEspecial());
        estudiante.setApoderadoIds(datos.getApoderadoIds());
        estudiante.setEstado(datos.getEstado());
        estudiante.setFechaActualizacion(LocalDateTime.now());

        return estudianteRepository.save(estudiante);
    }

    public void eliminar(String id) {
        estudianteRepository.delete(obtenerPorId(id));
    }
}