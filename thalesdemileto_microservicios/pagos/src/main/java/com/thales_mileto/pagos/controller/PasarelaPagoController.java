package com.thales_mileto.pagos.controller;

import com.thales_mileto.pagos.model.PasarelaPago;
import com.thales_mileto.pagos.service.PasarelaPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pasarelas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PasarelaPagoController {

    private final PasarelaPagoService pasarelaPagoService;

    @GetMapping
    public ResponseEntity<List<PasarelaPago>> listarTodas() {
        return ResponseEntity.ok(
                pasarelaPagoService.listarTodas()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PasarelaPago> buscarPorId(
            @PathVariable String id
    ) {
        return pasarelaPagoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/proveedor/{proveedor}")
    public ResponseEntity<PasarelaPago> buscarPorProveedor(
            @PathVariable String proveedor
    ) {
        return pasarelaPagoService.buscarPorProveedor(proveedor)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<PasarelaPago>> buscarActivas() {
        return ResponseEntity.ok(
                pasarelaPagoService.buscarActivas()
        );
    }

    @GetMapping("/inactivas")
    public ResponseEntity<List<PasarelaPago>> buscarInactivas() {
        return ResponseEntity.ok(
                pasarelaPagoService.buscarInactivas()
        );
    }

    @GetMapping("/modo/{modo}")
    public ResponseEntity<List<PasarelaPago>> buscarPorModo(
            @PathVariable String modo
    ) {
        return ResponseEntity.ok(
                pasarelaPagoService.buscarPorModo(modo)
        );
    }

    @GetMapping("/activas/modo/{modo}")
    public ResponseEntity<List<PasarelaPago>> buscarActivasPorModo(
            @PathVariable String modo
    ) {
        return ResponseEntity.ok(
                pasarelaPagoService.buscarActivasPorModo(modo)
        );
    }

    @GetMapping("/proveedor/{proveedor}/activo")
    public ResponseEntity<PasarelaPago> buscarProveedorActivo(
            @PathVariable String proveedor
    ) {
        return pasarelaPagoService
                .buscarProveedorActivo(proveedor)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> guardar(
            @RequestBody PasarelaPago pasarela
    ) {
        try {

            return ResponseEntity.ok(
                    pasarelaPagoService.guardar(pasarela)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable String id,
            @RequestBody PasarelaPago pasarela
    ) {
        try {

            return ResponseEntity.ok(
                    pasarelaPagoService.actualizar(
                            id,
                            pasarela
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<?> activar(
            @PathVariable String id
    ) {
        try {

            return ResponseEntity.ok(
                    pasarelaPagoService.activar(id)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivar(
            @PathVariable String id
    ) {
        try {

            return ResponseEntity.ok(
                    pasarelaPagoService.desactivar(id)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable String id
    ) {
        try {

            pasarelaPagoService.eliminar(id);

            return ResponseEntity.ok(
                    "Pasarela eliminada correctamente."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }
}