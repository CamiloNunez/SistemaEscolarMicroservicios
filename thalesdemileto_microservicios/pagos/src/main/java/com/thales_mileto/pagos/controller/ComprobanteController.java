package com.thales_mileto.pagos.controller;

import com.thales_mileto.pagos.model.Comprobante;
import com.thales_mileto.pagos.service.ComprobanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    @GetMapping
    public ResponseEntity<List<Comprobante>> listarTodos() {
        return ResponseEntity.ok(
                comprobanteService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comprobante> buscarPorId(
            @PathVariable String id
    ) {
        return comprobanteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pago/{pagoId}")
    public ResponseEntity<Comprobante> buscarPorPagoId(
            @PathVariable String pagoId
    ) {
        return comprobanteService.buscarPorPagoId(pagoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/numero/{numeroComprobante}")
    public ResponseEntity<Comprobante> buscarPorNumero(
            @PathVariable String numeroComprobante
    ) {
        return comprobanteService
                .buscarPorNumeroComprobante(numeroComprobante)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tipo/{tipoComprobante}")
    public ResponseEntity<List<Comprobante>> buscarPorTipo(
            @PathVariable String tipoComprobante
    ) {
        return ResponseEntity.ok(
                comprobanteService.buscarPorTipo(tipoComprobante)
        );
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Comprobante>> buscarPorEstado(
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                comprobanteService.buscarPorEstado(estado)
        );
    }

    @GetMapping("/filtro")
    public ResponseEntity<List<Comprobante>> buscarPorTipoYEstado(
            @RequestParam String tipo,
            @RequestParam String estado
    ) {
        return ResponseEntity.ok(
                comprobanteService.buscarPorTipoYEstado(
                        tipo,
                        estado
                )
        );
    }

    @PostMapping
    public ResponseEntity<?> guardar(
            @RequestBody Comprobante comprobante
    ) {
        try {
            return ResponseEntity.ok(
                    comprobanteService.guardar(comprobante)
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
            @RequestBody Comprobante comprobante
    ) {
        try {
            return ResponseEntity.ok(
                    comprobanteService.actualizar(
                            id,
                            comprobante
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<?> anular(
            @PathVariable String id
    ) {
        try {
            return ResponseEntity.ok(
                    comprobanteService.anular(id)
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

            comprobanteService.eliminar(id);

            return ResponseEntity.ok(
                    "Comprobante eliminado correctamente."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }
}