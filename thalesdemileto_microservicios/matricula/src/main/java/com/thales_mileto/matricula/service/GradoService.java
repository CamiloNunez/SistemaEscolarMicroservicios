package com.thales_mileto.matricula.service;

import com.thales_mileto.matricula.model.Grado;
import com.thales_mileto.matricula.repository.GradoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GradoService {

    private final GradoRepository gradoRepository;

    public List<Grado> listar() {
        return gradoRepository.findAll();
    }

    public Grado obtenerPorId(String id) {
        return gradoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grado no encontrado"));
    }

    public Grado crear(Grado grado, Principal principal) {
        grado.setId(null);

        if (gradoRepository.existsByProgramaAcademicoIdAndNumero(
                grado.getProgramaAcademicoId(),
                grado.getNumero()
        )) {
            throw new RuntimeException("Ya existe ese grado para el programa académico");
        }

        grado.setEstado("ACTIVO");
        grado.setUsuarioRegistro(principal.getName());
        grado.setFechaRegistro(LocalDateTime.now());
        grado.setFechaActualizacion(LocalDateTime.now());

        return gradoRepository.save(grado);
    }

    public Grado actualizar(String id, Grado datos) {
        Grado grado = obtenerPorId(id);

        grado.setProgramaAcademicoId(datos.getProgramaAcademicoId());
        grado.setNumero(datos.getNumero());
        grado.setNombre(datos.getNombre());
        grado.setDescripcion(datos.getDescripcion());
        grado.setEdadMinima(datos.getEdadMinima());
        grado.setEdadMaxima(datos.getEdadMaxima());
        grado.setEstado(datos.getEstado());
        grado.setFechaActualizacion(LocalDateTime.now());

        return gradoRepository.save(grado);
    }

    public void eliminar(String id) {
        gradoRepository.delete(obtenerPorId(id));
    }
}