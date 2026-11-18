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
@Table(name = "intentos_pago")
public class IntentoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    private String pagoId;

    private String deudaId;

    private String proveedor; // Ej: STRIPE, CULQI, MERCADOPAGO

    @Column(unique = true)
    private String paymentIntentId;

    @Column(precision = 15, scale = 2)
    private BigDecimal monto;

    private String estado; // Ej: CREADO, PROCESANDO, EXITOSO, FALLIDO

    private String mensajeRespuesta;

    private LocalDateTime fechaIntento;
}
