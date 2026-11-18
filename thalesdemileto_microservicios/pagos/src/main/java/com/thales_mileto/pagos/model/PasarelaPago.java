package com.thales_mileto.pagos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pasarelas_pago")
public class PasarelaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    private String nombre;

    private String proveedor; // Ej: STRIPE, CULQI, MERCADOPAGO

    private Boolean activo = true;

    private String modo; // Ej: SANDBOX, PRODUCCION

    private LocalDateTime fechaRegistro;
}

