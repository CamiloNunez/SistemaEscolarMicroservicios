package com.thales_mileto.matricula.controller;

import com.thales_mileto.matricula.model.Apoderado;
import com.thales_mileto.matricula.service.ApoderadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/apoderados")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApoderadoController {

    private final ApoderadoService apoderadoService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(apoderadoService.listar());
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(apoderadoService.buscarPorNombre(nombre));
    }

    /** Apoderados sin usuario de login asignado (pendientes de vincular). */
    @GetMapping("/sin-usuario")
    public ResponseEntity<?> listarSinUsuario() {
        return ResponseEntity.ok(apoderadoService.listarSinUsuario());
    }

    /** Apoderados cuyo usuario vinculado no coincide con login. */
    @GetMapping("/vinculos/verificar")
    public ResponseEntity<?> verificarVinculos() {
        return ResponseEntity.ok(apoderadoService.verificarVinculos());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(apoderadoService.obtenerPorUsuarioId(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(apoderadoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Apoderado apoderado, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apoderadoService.crear(apoderado, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Apoderado apoderado) {
        return ResponseEntity.ok(apoderadoService.actualizar(id, apoderado));
    }

    /** Vincula el apoderado con un usuario PADRE de login (valida tipo y DNI). */
    @PutMapping("/{id}/usuario/{usuarioId}")
    public ResponseEntity<?> vincularUsuario(@PathVariable String id, @PathVariable String usuarioId) {
        return ResponseEntity.ok(apoderadoService.vincularUsuario(id, usuarioId));
    }

    @DeleteMapping("/{id}/usuario")
    public ResponseEntity<?> desvincularUsuario(@PathVariable String id) {
        return ResponseEntity.ok(apoderadoService.desvincularUsuario(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        apoderadoService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Apoderado eliminado correctamente"));
    }
}
