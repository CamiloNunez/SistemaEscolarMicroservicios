package com.thales_mileto.matricula.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "pagos_matricula",
        uniqueConstraints = @UniqueConstraint(name = "uk_pago_matricula_comprobante",
                columnNames = "numero_comprobante"),
        indexes = {
                @Index(name = "idx_pago_matricula", columnList = "matricula_id"),
                @Index(name = "idx_pago_matricula_estado", columnList = "estado")
        })
public class PagoMatricula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "matricula_id")
    private String matriculaId;

    @Column(name = "numero_comprobante")
    private String numeroComprobante;    // número de recibo o boleta

    // "DERECHO_MATRICULA" | "PENSION" | "SEGURO" | "MATERIALES" | "OTRO"
    private String concepto;

    @Column(precision = 15, scale = 2)
    private BigDecimal monto;
    private String moneda;               // "PEN" | "USD"

    // "EFECTIVO" | "TRANSFERENCIA" | "TARJETA" | "DEPOSITO" | "YAPE" | "PLIN"
    private String metodoPago;

    private LocalDate fechaVencimiento;
    private LocalDate fechaPago;

    private String referenciaBancaria;
    private String entidadBancaria;

    // "PENDIENTE" | "PAGADO" | "ANULADO" | "DEVUELTO"
    private String estado;

    @Column(length = 2000)
    private String observaciones;
    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
