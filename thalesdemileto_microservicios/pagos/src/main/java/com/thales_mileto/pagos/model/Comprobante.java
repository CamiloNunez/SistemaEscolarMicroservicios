package com.thales_mileto.pagos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comprobantes")
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    private String pagoId;

    @Column(unique = true)
    private String numeroComprobante;

    private String tipoComprobante; // Ej: BOLETA, FACTURA, RECIBO

    @Column(precision = 15, scale = 2)
    private BigDecimal monto;

    private LocalDateTime fechaEmision;

    private String urlDocumento;

    private String estado; // Ej: EMITIDO, ANULADO
}