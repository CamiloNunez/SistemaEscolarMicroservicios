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
@Table(name = "deudas")
public class Deuda {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    private String estudianteId;

    private String matriculaId;

    private String concepto; // Ej: MATRICULA, PENSION, MATERIAL

    @Column(precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(precision = 15, scale = 2)
    private BigDecimal saldoPendiente;

    private String moneda; // Ej: PEN, USD

    private LocalDateTime fechaEmision;

    private LocalDateTime fechaVencimiento;

    private String estado; // Ej: PENDIENTE, PARCIAL, PAGADA, VENCIDA, ANULADA

    private LocalDateTime fechaRegistro;
}
