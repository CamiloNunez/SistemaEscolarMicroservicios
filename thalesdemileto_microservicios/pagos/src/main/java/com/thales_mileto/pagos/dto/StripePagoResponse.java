package com.thales_mileto.pagos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StripePagoResponse {
    private String pagoId;
    private String paymentIntentId;
    private String clientSecret;
}