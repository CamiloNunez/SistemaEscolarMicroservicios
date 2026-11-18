package com.thales_mileto.notificaciones.config;

import com.thales_mileto.notificaciones.model.*;
import com.thales_mileto.notificaciones.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PlantillaNotificacionRepository plantillaRepository;
    private final NotificacionRepository notificacionRepository;
    private final DestinatarioNotificacionRepository destinatarioRepository;
    private final PreferenciaNotificacionRepository preferenciaRepository;

    @Override
    public void run(String... args) {
        if (plantillaRepository.count() > 0) {
            System.out.println(">>> Seeder notificaciones omitido: ya existen datos");
            return;
        }

        System.out.println(">>> Ejecutando Seeder notificaciones...");

        LocalDateTime ahora = LocalDateTime.now();

        // IDs funcionales usados también por matrícula, académico y pagos.
        String estudianteJuan = "EST001";
        String estudianteMaria = "EST002";
        String estudiantePedro = "EST003";
        String docenteMatematica = "USR-DOC-001";

        String matriculaJuan = "MAT-2026-001";
        String matriculaMaria = "MAT-2026-002";
        String referenciaPagoMaria = "YAPE-987654";
        String cursoMatematica = "MAT";
        String cursoComunicacion = "COM";

        // =========================================================
        // 1. PLANTILLAS
        // =========================================================
        PlantillaNotificacion matriculaConfirmada = new PlantillaNotificacion(
                null,
                "MATRICULA_CONFIRMADA",
                "Matrícula confirmada",
                "Matrícula confirmada",
                "Tu matrícula para el periodo 2026 fue confirmada correctamente.",
                CanalNotificacion.EMAIL,
                "MATRICULA_CONFIRMADA",
                true,
                ahora
        );

        PlantillaNotificacion pagoConfirmado = new PlantillaNotificacion(
                null,
                "PAGO_CONFIRMADO",
                "Pago confirmado",
                "Pago confirmado",
                "Tu pago fue procesado y confirmado correctamente.",
                CanalNotificacion.EMAIL,
                "PAGO_CONFIRMADO",
                true,
                ahora
        );

        PlantillaNotificacion pagoPendiente = new PlantillaNotificacion(
                null,
                "PAGO_PENDIENTE",
                "Pago pendiente",
                "Pago pendiente de matrícula",
                "Tienes un pago de matrícula pendiente por S/ 350.00.",
                CanalNotificacion.EMAIL,
                "PAGO_PENDIENTE",
                true,
                ahora
        );

        PlantillaNotificacion notaRegistrada = new PlantillaNotificacion(
                null,
                "NOTA_REGISTRADA",
                "Nota registrada",
                "Nueva calificación registrada",
                "Se registró una nueva calificación en tu expediente académico.",
                CanalNotificacion.PUSH,
                "NOTA_REGISTRADA",
                true,
                ahora
        );

        PlantillaNotificacion tardanzaRegistrada = new PlantillaNotificacion(
                null,
                "TARDANZA_REGISTRADA",
                "Tardanza registrada",
                "Registro de tardanza",
                "Se registró una tardanza en la asistencia del estudiante.",
                CanalNotificacion.PUSH,
                "ASISTENCIA_TARDANZA",
                true,
                ahora
        );

        plantillaRepository.saveAll(List.of(
                matriculaConfirmada,
                pagoConfirmado,
                pagoPendiente,
                notaRegistrada,
                tardanzaRegistrada
        ));

        // =========================================================
        // 2. PREFERENCIAS
        // =========================================================
        PreferenciaNotificacion prefJuan = new PreferenciaNotificacion(
                null, estudianteJuan, true, true, true, true, true, ahora
        );

        PreferenciaNotificacion prefMaria = new PreferenciaNotificacion(
                null, estudianteMaria, true, true, true, true, true, ahora
        );

        PreferenciaNotificacion prefPedro = new PreferenciaNotificacion(
                null, estudiantePedro, true, true, true, true, true, ahora
        );

        PreferenciaNotificacion prefDocente = new PreferenciaNotificacion(
                null, docenteMatematica, true, true, false, false, true, ahora
        );

        preferenciaRepository.saveAll(List.of(prefJuan, prefMaria, prefPedro, prefDocente));

        // =========================================================
        // 3. NOTIFICACIONES
        // =========================================================
        Notificacion notifMatriculaJuan = new Notificacion(
                null,
                matriculaConfirmada.getId(),
                "MATRICULA_CONFIRMADA",
                matriculaJuan,
                CanalNotificacion.EMAIL,
                "Matrícula confirmada - Juan Quispe Mamani",
                "La matrícula de Juan Quispe Mamani para Primer Grado, sección 1° A, periodo 2026 fue confirmada.",
                "ENVIADA",
                ahora.minusDays(5),
                ahora.minusDays(5).plusMinutes(1)
        );

        Notificacion notifMatriculaMaria = new Notificacion(
                null,
                matriculaConfirmada.getId(),
                "MATRICULA_CONFIRMADA",
                matriculaMaria,
                CanalNotificacion.EMAIL,
                "Matrícula confirmada - María Quispe Mamani",
                "La matrícula de María Quispe Mamani para Segundo Grado, sección 2° A, periodo 2026 fue confirmada.",
                "ENVIADA",
                ahora.minusDays(4),
                ahora.minusDays(4).plusMinutes(1)
        );

        Notificacion notifPagoMaria = new Notificacion(
                null,
                pagoConfirmado.getId(),
                "PAGO_CONFIRMADO",
                referenciaPagoMaria,
                CanalNotificacion.EMAIL,
                "Pago de pensión confirmado",
                "Se confirmó el pago de S/ 500.00 de María Quispe Mamani mediante Yape. Referencia: YAPE-987654.",
                "ENVIADA",
                ahora.minusDays(3),
                ahora.minusDays(3).plusMinutes(1)
        );

        Notificacion notifPagoJuan = new Notificacion(
                null,
                pagoPendiente.getId(),
                "PAGO_PENDIENTE",
                matriculaJuan,
                CanalNotificacion.EMAIL,
                "Pago de matrícula pendiente",
                "Juan Quispe Mamani mantiene pendiente S/ 350.00 por concepto de matrícula.",
                "PENDIENTE",
                ahora.minusDays(2),
                null
        );

        Notificacion notifNotaJuan = new Notificacion(
                null,
                notaRegistrada.getId(),
                "NOTA_REGISTRADA",
                estudianteJuan + ":" + cursoMatematica + ":2026:PARCIAL",
                CanalNotificacion.PUSH,
                "Nueva nota de Matemática",
                "Juan Quispe Mamani obtuvo 18.5 en la evaluación parcial del primer bimestre de Matemática.",
                "ENVIADA",
                ahora.minusDays(1),
                ahora.minusDays(1).plusSeconds(30)
        );

        Notificacion notifNotaMaria = new Notificacion(
                null,
                notaRegistrada.getId(),
                "NOTA_REGISTRADA",
                estudianteMaria + ":" + cursoComunicacion + ":2026:FINAL",
                CanalNotificacion.PUSH,
                "Nueva nota de Comunicación",
                "María Quispe Mamani obtuvo 17.0 en la evaluación final de Comunicación.",
                "ENVIADA",
                ahora.minusHours(12),
                ahora.minusHours(12).plusSeconds(30)
        );

        Notificacion notifTardanzaPedro = new Notificacion(
                null,
                tardanzaRegistrada.getId(),
                "ASISTENCIA_TARDANZA",
                estudiantePedro + ":CTA:1° A Secundaria:2026",
                CanalNotificacion.PUSH,
                "Tardanza registrada - Pedro Fernández Rojas",
                "Se registró una tardanza de Pedro Fernández Rojas en Ciencia y Tecnología.",
                "PENDIENTE",
                ahora.minusHours(2),
                null
        );

        notificacionRepository.saveAll(List.of(
                notifMatriculaJuan,
                notifMatriculaMaria,
                notifPagoMaria,
                notifPagoJuan,
                notifNotaJuan,
                notifNotaMaria,
                notifTardanzaPedro
        ));

        // =========================================================
        // 4. DESTINATARIOS
        // =========================================================
        destinatarioRepository.saveAll(List.of(
                new DestinatarioNotificacion(null, notifMatriculaJuan.getId(), estudianteJuan,
                        "juan.quispe@example.com", "ESTUDIANTE", "ENTREGADO", notifMatriculaJuan.getFechaEnvio()),

                new DestinatarioNotificacion(null, notifMatriculaMaria.getId(), estudianteMaria,
                        "maria.quispe@example.com", "ESTUDIANTE", "ENTREGADO", notifMatriculaMaria.getFechaEnvio()),

                new DestinatarioNotificacion(null, notifPagoMaria.getId(), estudianteMaria,
                        "maria.quispe@example.com", "ESTUDIANTE", "ENTREGADO", notifPagoMaria.getFechaEnvio()),

                new DestinatarioNotificacion(null, notifPagoJuan.getId(), estudianteJuan,
                        "juan.quispe@example.com", "ESTUDIANTE", "PENDIENTE", null),

                new DestinatarioNotificacion(null, notifNotaJuan.getId(), estudianteJuan,
                        "push-token-est001-demo", "ESTUDIANTE", "ENTREGADO", notifNotaJuan.getFechaEnvio()),

                new DestinatarioNotificacion(null, notifNotaMaria.getId(), estudianteMaria,
                        "push-token-est002-demo", "ESTUDIANTE", "ENTREGADO", notifNotaMaria.getFechaEnvio()),

                new DestinatarioNotificacion(null, notifTardanzaPedro.getId(), estudiantePedro,
                        "push-token-est003-demo", "ESTUDIANTE", "PENDIENTE", null)
        ));

        System.out.println(">>> Seeder notificaciones completado correctamente");
    }
}
