package com.thales_mileto.login.controller;

import com.thales_mileto.login.dto.RolDTO;
import com.thales_mileto.login.model.Rol;
import com.thales_mileto.login.service.RolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    public ResponseEntity<List<Rol>> listar() {
        return ResponseEntity.ok(rolService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable String id) {
        try {
            return ResponseEntity.ok(rolService.obtener(id));
        } catch (Exception e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody RolDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crear(dto));
        } catch (Exception e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @Valid @RequestBody RolDTO dto) {
        try {
            return ResponseEntity.ok(rolService.actualizar(id, dto));
        } catch (Exception e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            rolService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Rol eliminado exitosamente"));
        } catch (Exception e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String mensaje) {
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
