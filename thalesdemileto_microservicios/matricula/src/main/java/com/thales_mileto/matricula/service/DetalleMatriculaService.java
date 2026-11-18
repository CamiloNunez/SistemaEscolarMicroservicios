package com.thales_mileto.matricula.service;

import com.thales_mileto.matricula.model.DetalleMatricula;
import com.thales_mileto.matricula.repository.DetalleMatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DetalleMatriculaService {

    private final DetalleMatriculaRepository detalleMatriculaRepository;

    public List<DetalleMatricula> listar() {
        return detalleMatriculaRepository.findAll();
    }

    public DetalleMatricula obtenerPorId(String id) {
        return detalleMatriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle de matrícula no encontrado"));
    }

    public DetalleMatricula crear(DetalleMatricula detalle, Principal principal) {
        detalle.setId(null);

        if (detalleMatriculaRepository.existsByMatriculaIdAndCursoId(
                detalle.getMatriculaId(),
                detalle.getCursoId()
        )) {
            throw new RuntimeException("El curso ya está registrado en esta matrícula");
        }

        if (detalle.getEstado() == null) {
            detalle.setEstado("INSCRITO");
        }

        detalle.setUsuarioRegistro(principal.getName());
        detalle.setFechaRegistro(LocalDateTime.now());
        detalle.setFechaActualizacion(LocalDateTime.now());

        return detalleMatriculaRepository.save(detalle);
    }

    public DetalleMatricula actualizar(String id, DetalleMatricula datos) {
        DetalleMatricula detalle = obtenerPorId(id);

        detalle.setMatriculaId(datos.getMatriculaId());
        detalle.setCursoId(datos.getCursoId());
        detalle.setNombreCurso(datos.getNombreCurso());
        detalle.setCodigoCurso(datos.getCodigoCurso());
        detalle.setHorasSemanales(datos.getHorasSemanales());
        detalle.setTipoInscripcion(datos.getTipoInscripcion());
        detalle.setEstado(datos.getEstado());
        detalle.setFechaActualizacion(LocalDateTime.now());

        return detalleMatriculaRepository.save(detalle);
    }

    public void eliminar(String id) {
        detalleMatriculaRepository.delete(obtenerPorId(id));
    }
}