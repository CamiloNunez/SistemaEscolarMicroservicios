package com.thales_mileto.pagos.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoEvent {
    private String evento;
    private String pagoId;
    private String deudaId;
    private String estudianteId;
    private String matriculaId;
    private BigDecimal monto;
    private String moneda;
    private String metodoPago;
    private LocalDateTime fechaPago;
    private String referenciaExterna;
    private String estado;
    private LocalDateTime fechaRegistro;
}
