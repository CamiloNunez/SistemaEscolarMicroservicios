package com.thales_mileto.matricula.service;

import com.thales_mileto.matricula.model.DocumentoMatricula;
import com.thales_mileto.matricula.repository.DocumentoMatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentoMatriculaService {

    private final DocumentoMatriculaRepository documentoMatriculaRepository;

    public List<DocumentoMatricula> listar() {
        return documentoMatriculaRepository.findAll();
    }

    public DocumentoMatricula obtenerPorId(String id) {
        return documentoMatriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento de matrícula no encontrado"));
    }

    public DocumentoMatricula crear(DocumentoMatricula documento, Principal principal) {
        documento.setId(null);

        if (documento.getEstado() == null) {
            documento.setEstado("PENDIENTE");
        }

        documento.setUsuarioRegistro(principal.getName());
        documento.setFechaRegistro(LocalDateTime.now());
        documento.setFechaActualizacion(LocalDateTime.now());

        return documentoMatriculaRepository.save(documento);
    }

    public DocumentoMatricula actualizar(String id, DocumentoMatricula datos) {
        DocumentoMatricula documento = obtenerPorId(id);

        documento.setMatriculaId(datos.getMatriculaId());
        documento.setTipoDocumento(datos.getTipoDocumento());
        documento.setNombreArchivo(datos.getNombreArchivo());
        documento.setRutaAlmacenamiento(datos.getRutaAlmacenamiento());
        documento.setUrlAcceso(datos.getUrlAcceso());
        documento.setMimeType(datos.getMimeType());
        documento.setTamañoBytes(datos.getTamañoBytes());
        documento.setFechaEmision(datos.getFechaEmision());
        documento.setFechaVencimiento(datos.getFechaVencimiento());
        documento.setEsObligatorio(datos.getEsObligatorio());
        documento.setEstado(datos.getEstado());
        documento.setObservaciones(datos.getObservaciones());
        documento.setUsuarioVerificacion(datos.getUsuarioVerificacion());
        documento.setFechaVerificacion(datos.getFechaVerificacion());
        documento.setFechaActualizacion(LocalDateTime.now());

        return documentoMatriculaRepository.save(documento);
    }

    public void eliminar(String id) {
        documentoMatriculaRepository.delete(obtenerPorId(id));
    }
}