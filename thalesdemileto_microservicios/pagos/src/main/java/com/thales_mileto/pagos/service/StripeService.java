package com.thales_mileto.pagos.service;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StripeService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    public PaymentIntent crearIntento(
            BigDecimal monto,
            String moneda,
            String pagoId,
            String deudaId
    ) throws Exception {

        com.stripe.Stripe.apiKey = secretKey;

        long amount = monto
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amount)
                        .setCurrency(moneda.toLowerCase())
                        .putMetadata("pagoId", pagoId)
                        .putMetadata("deudaId", deudaId)
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods
                                .builder()
                                .setEnabled(true)
                                .build()
                        )
                        .build();

        return PaymentIntent.create(params);
    }
}