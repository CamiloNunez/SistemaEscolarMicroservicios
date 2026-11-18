package com.thales_mileto.matricula.service;

import com.thales_mileto.matricula.model.Seccion;
import com.thales_mileto.matricula.repository.SeccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SeccionService {

    private final SeccionRepository seccionRepository;

    public List<Seccion> listar() {
        return seccionRepository.findAll();
    }

    public Seccion obtenerPorId(String id) {
        return seccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sección no encontrada"));
    }

    public Seccion crear(Seccion seccion, Principal principal) {
        seccion.setId(null);

        if (seccionRepository.existsByGradoIdAndLetraAndPeriodoAcademicoId(
                seccion.getGradoId(),
                seccion.getLetra(),
                seccion.getPeriodoAcademicoId()
        )) {
            throw new RuntimeException("Ya existe esa sección para el grado y periodo académico");
        }

        seccion.setEstado("ACTIVO");
        seccion.setUsuarioRegistro(principal.getName());
        seccion.setFechaRegistro(LocalDateTime.now());
        seccion.setFechaActualizacion(LocalDateTime.now());

        return seccionRepository.save(seccion);
    }

    public Seccion actualizar(String id, Seccion datos) {
        Seccion seccion = obtenerPorId(id);

        seccion.setGradoId(datos.getGradoId());
        seccion.setPeriodoAcademicoId(datos.getPeriodoAcademicoId());
        seccion.setLetra(datos.getLetra());
        seccion.setNombre(datos.getNombre());
        seccion.setTurno(datos.getTurno());
        seccion.setCapacidadMaxima(datos.getCapacidadMaxima());
        seccion.setAulaAsignada(datos.getAulaAsignada());
        seccion.setDocenteTutorId(datos.getDocenteTutorId());
        seccion.setEstado(datos.getEstado());
        seccion.setFechaActualizacion(LocalDateTime.now());

        return seccionRepository.save(seccion);
    }

    public void eliminar(String id) {
        seccionRepository.delete(obtenerPorId(id));
    }
}