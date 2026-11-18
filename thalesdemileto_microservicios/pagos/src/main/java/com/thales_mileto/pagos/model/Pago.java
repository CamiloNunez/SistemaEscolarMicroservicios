package com.thales_mileto.pagos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Reemplaza conceptualmente a PagoMatricula.java (antes en el microservicio matricula).
 * matricula debería quedarse solo con matriculaId + estado derivado del pago,
 * consumiendo este microservicio como fuente de verdad para todo lo referente al pago.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    private String deudaId;

    private String estudianteId;

    private String matriculaId;

    @Column(precision = 15, scale = 2)
    private BigDecimal monto;

    private String moneda;

    private String metodoPago; // Ej: TARJETA, TRANSFERENCIA, EFECTIVO, YAPE, PLIN

    private LocalDateTime fechaPago;

    @Column(unique = true)
    private String referenciaExterna;

    private String estado; // Ej: PENDIENTE, CONFIRMADO, RECHAZADO, REEMBOLSADO

    private LocalDateTime fechaRegistro;
}