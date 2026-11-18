package com.thales_mileto.pagos.config;

import com.thales_mileto.pagos.model.*;
import com.thales_mileto.pagos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DeudaRepository deudaRepository;
    private final PagoRepository pagoRepository;
    private final IntentoPagoRepository intentoPagoRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final PasarelaPagoRepository pasarelaPagoRepository;


    @Override
    @Transactional
    public void run(String... args) {


        if (deudaRepository.count() > 0) {
            System.out.println(">>> Seeder pagos omitido: ya existen datos");
            return;
        }


        System.out.println(">>> Ejecutando Seeder pagos...");


        // IDs simulados provenientes de matrícula
        String estudianteJuan = "EST001";
        String estudianteMaria = "EST002";

        String matriculaJuan = "MAT-2026-001";
        String matriculaMaria = "MAT-2026-002";


        // ============================
        // PASARELAS
        // ============================

        PasarelaPago culqi =
                new PasarelaPago();

        culqi.setNombre("Culqi Sandbox");
        culqi.setProveedor("CULQI");
        culqi.setActivo(true);
        culqi.setModo("SANDBOX");
        culqi.setFechaRegistro(LocalDateTime.now());


        PasarelaPago yape =
                new PasarelaPago();

        yape.setNombre("Yape Escolar");
        yape.setProveedor("YAPE");
        yape.setActivo(true);
        yape.setModo("PRODUCCION");
        yape.setFechaRegistro(LocalDateTime.now());


        pasarelaPagoRepository.saveAll(
                List.of(culqi, yape)
        );


        // ============================
        // DEUDAS
        // ============================

        Deuda deudaJuan = new Deuda();

        deudaJuan.setEstudianteId(estudianteJuan);
        deudaJuan.setMatriculaId(matriculaJuan);
        deudaJuan.setConcepto("MATRICULA");
        deudaJuan.setMonto(new BigDecimal("350.00"));
        deudaJuan.setSaldoPendiente(new BigDecimal("350.00"));
        deudaJuan.setMoneda("PEN");
        deudaJuan.setFechaEmision(LocalDateTime.now());
        deudaJuan.setFechaVencimiento(
                LocalDateTime.now().plusDays(15)
        );
        deudaJuan.setEstado("PENDIENTE");
        deudaJuan.setFechaRegistro(LocalDateTime.now());



        Deuda deudaMaria = new Deuda();

        deudaMaria.setEstudianteId(estudianteMaria);
        deudaMaria.setMatriculaId(matriculaMaria);
        deudaMaria.setConcepto("PENSION");
        deudaMaria.setMonto(new BigDecimal("500.00"));
        deudaMaria.setSaldoPendiente(new BigDecimal("0.00"));
        deudaMaria.setMoneda("PEN");
        deudaMaria.setFechaEmision(LocalDateTime.now());
        deudaMaria.setFechaVencimiento(
                LocalDateTime.now().minusDays(5)
        );
        deudaMaria.setEstado("PAGADA");
        deudaMaria.setFechaRegistro(LocalDateTime.now());


        deudaRepository.saveAll(
                List.of(deudaJuan, deudaMaria)
        );


        // ============================
        // PAGOS
        // ============================

        Pago pagoMaria = new Pago();

        pagoMaria.setDeudaId(deudaMaria.getId());
        pagoMaria.setEstudianteId(estudianteMaria);
        pagoMaria.setMatriculaId(matriculaMaria);
        pagoMaria.setMonto(new BigDecimal("500.00"));
        pagoMaria.setMoneda("PEN");
        pagoMaria.setMetodoPago("YAPE");
        pagoMaria.setFechaPago(LocalDateTime.now());
        pagoMaria.setReferenciaExterna("YAPE-987654");
        pagoMaria.setEstado("CONFIRMADO");
        pagoMaria.setFechaRegistro(LocalDateTime.now());


        pagoRepository.save(pagoMaria);



        // ============================
        // INTENTO DE PAGO
        // ============================

        IntentoPago intento = new IntentoPago();

        intento.setPagoId(pagoMaria.getId());
        intento.setDeudaId(deudaMaria.getId());
        intento.setProveedor("YAPE");
        intento.setPaymentIntentId(
                "PAYMENT-INTENT-001"
        );
        intento.setMonto(
                new BigDecimal("500.00")
        );
        intento.setEstado("EXITOSO");
        intento.setMensajeRespuesta(
                "Pago procesado correctamente"
        );
        intento.setFechaIntento(
                LocalDateTime.now()
        );


        intentoPagoRepository.save(intento);



        // ============================
        // COMPROBANTE
        // ============================

        Comprobante comprobante =
                new Comprobante();

        comprobante.setPagoId(
                pagoMaria.getId()
        );
        comprobante.setNumeroComprobante(
                "B001-000001"
        );
        comprobante.setTipoComprobante(
                "BOLETA"
        );
        comprobante.setMonto(
                new BigDecimal("500.00")
        );
        comprobante.setFechaEmision(
                LocalDateTime.now()
        );
        comprobante.setUrlDocumento(
                "/documentos/boleta001.pdf"
        );
        comprobante.setEstado(
                "EMITIDO"
        );


        comprobanteRepository.save(comprobante);



        System.out.println(
                ">>> Seeder pagos completado correctamente"
        );

    }
}