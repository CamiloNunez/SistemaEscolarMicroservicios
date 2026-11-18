package com.thales_mileto.matricula.service;

import com.thales_mileto.matricula.model.Curso;
import com.thales_mileto.matricula.repository.CursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

    public List<Curso> listar() {
        return cursoRepository.findAll();
    }

    public Curso obtenerPorId(String id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
    }

    public Curso crear(Curso curso, Principal principal) {
        curso.setId(null);

        if (curso.getCodigo() != null && cursoRepository.existsByCodigo(curso.getCodigo())) {
            throw new RuntimeException("Ya existe un curso con ese código");
        }

        curso.setEstado("ACTIVO");
        curso.setUsuarioRegistro(principal.getName());
        curso.setFechaRegistro(LocalDateTime.now());
        curso.setFechaActualizacion(LocalDateTime.now());

        return cursoRepository.save(curso);
    }

    public Curso actualizar(String id, Curso datos) {
        Curso curso = obtenerPorId(id);

        curso.setCodigo(datos.getCodigo());
        curso.setNombre(datos.getNombre());
        curso.setDescripcion(datos.getDescripcion());
        curso.setArea(datos.getArea());
        curso.setTipo(datos.getTipo());
        curso.setHorasSemanales(datos.getHorasSemanales());
        curso.setEstado(datos.getEstado());
        curso.setFechaActualizacion(LocalDateTime.now());

        return cursoRepository.save(curso);
    }

    public void eliminar(String id) {
        cursoRepository.delete(obtenerPorId(id));
    }
}