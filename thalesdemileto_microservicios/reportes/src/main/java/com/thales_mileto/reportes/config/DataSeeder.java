package com.thales_mileto.reportes.config;

import com.thales_mileto.reportes.model.EventoAuditoria;
import com.thales_mileto.reportes.model.Indicador;
import com.thales_mileto.reportes.model.ReporteGenerado;
import com.thales_mileto.reportes.repository.EventoAuditoriaRepository;
import com.thales_mileto.reportes.repository.IndicadorRepository;
import com.thales_mileto.reportes.repository.ReporteGeneradoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final IndicadorRepository indicadorRepository;
    private final EventoAuditoriaRepository eventoAuditoriaRepository;
    private final ReporteGeneradoRepository reporteGeneradoRepository;

    @Override
    public void run(String... args) {

        if (indicadorRepository.count() > 0
                || eventoAuditoriaRepository.count() > 0
                || reporteGeneradoRepository.count() > 0) {
            System.out.println(">>> Seeder reportes omitido: ya existen datos");
            return;
        }

        System.out.println(">>> Ejecutando Seeder reportes...");

        LocalDateTime ahora = LocalDateTime.now();

        // Identificadores funcionales compartidos con los otros microservicios.
        String admin = "admin";
        String profesor01 = "profesor01";
        String docenteMatematica = "USR-DOC-001";
        String docenteComunicacion = "USR-DOC-002";

        String estudianteJuan = "EST001";
        String estudianteMaria = "EST002";
        String estudiantePedro = "EST003";

        String matriculaJuan = "MAT-2026-001";
        String matriculaMaria = "MAT-2026-002";
        String matriculaPedro = "MAT-2026-003";

        String matematica = "MAT";
        String comunicacion = "COM";
        String ciencia = "CTA";
        String periodo2026 = "2026";

        // =========================================================
        // 1. INDICADORES
        // =========================================================
        Indicador disponibilidad = new Indicador(
                null,
                "DISPONIBILIDAD",
                "Disponibilidad de microservicios",
                "DISPONIBILIDAD",
                99.80,
                "2026-08",
                ahora.minusMinutes(30)
        );

        Indicador tiempoRespuesta = new Indicador(
                null,
                "TIEMPO_RESPUESTA",
                "Tiempo promedio de respuesta",
                "RENDIMIENTO",
                185.0,
                "2026-08",
                ahora.minusMinutes(25)
        );

        Indicador tasaError = new Indicador(
                null,
                "TASA_ERROR",
                "Tasa de errores",
                "RENDIMIENTO",
                0.70,
                "2026-08",
                ahora.minusMinutes(20)
        );

        Indicador operaciones = new Indicador(
                null,
                "OPERACIONES_PROCESADAS",
                "Operaciones procesadas",
                "USO",
                27.0,
                "2026-08",
                ahora.minusMinutes(15)
        );

        Indicador matriculasConfirmadas = new Indicador(
                null,
                "MATRICULAS_CONFIRMADAS",
                "Matrículas confirmadas",
                "NEGOCIO",
                2.0,
                periodo2026,
                ahora.minusMinutes(10)
        );

        Indicador pagosConfirmados = new Indicador(
                null,
                "PAGOS_CONFIRMADOS",
                "Pagos confirmados",
                "NEGOCIO",
                1.0,
                "2026-08",
                ahora.minusMinutes(8)
        );

        Indicador promedioNotas = new Indicador(
                null,
                "PROMEDIO_NOTAS",
                "Promedio de notas registradas",
                "NEGOCIO",
                17.75,
                periodo2026,
                ahora.minusMinutes(5)
        );

        indicadorRepository.saveAll(List.of(
                disponibilidad,
                tiempoRespuesta,
                tasaError,
                operaciones,
                matriculasConfirmadas,
                pagosConfirmados,
                promedioNotas
        ));

        // =========================================================
        // 2. EVENTOS DE AUDITORÍA
        // =========================================================
        EventoAuditoria matriculaCreadaJuan = evento(
                admin,
                "matricula",
                "MATRICULA_CREADA",
                "Matricula",
                matriculaJuan,
                "CREAR",
                map(
                        "estudianteId", estudianteJuan,
                        "nombreEstudiante", "Juan Quispe Mamani",
                        "grado", "Primer Grado",
                        "seccion", "1° A",
                        "periodo", periodo2026,
                        "estado", "CONFIRMADA"
                ),
                "10.10.100.21",
                ahora.minusDays(5),
                "CORR-MAT-2026-001"
        );

        EventoAuditoria matriculaCreadaMaria = evento(
                admin,
                "matricula",
                "MATRICULA_CREADA",
                "Matricula",
                matriculaMaria,
                "CREAR",
                map(
                        "estudianteId", estudianteMaria,
                        "nombreEstudiante", "María Quispe Mamani",
                        "grado", "Segundo Grado",
                        "seccion", "2° A",
                        "periodo", periodo2026,
                        "estado", "CONFIRMADA"
                ),
                "10.10.100.21",
                ahora.minusDays(4),
                "CORR-MAT-2026-002"
        );

        EventoAuditoria matriculaPendientePedro = evento(
                admin,
                "matricula",
                "MATRICULA_CREADA",
                "Matricula",
                matriculaPedro,
                "CREAR",
                map(
                        "estudianteId", estudiantePedro,
                        "nombreEstudiante", "Pedro Fernández Rojas",
                        "grado", "Primer Año de Secundaria",
                        "seccion", "1° A Secundaria",
                        "periodo", periodo2026,
                        "estado", "PENDIENTE"
                ),
                "10.10.100.21",
                ahora.minusDays(3).minusHours(6),
                "CORR-MAT-2026-003"
        );

        EventoAuditoria pagoConfirmadoMaria = evento(
                estudianteMaria,
                "pagos",
                "PAGO_CONFIRMADO",
                "Pago",
                "YAPE-987654",
                "ACTUALIZAR",
                map(
                        "estudianteId", estudianteMaria,
                        "matriculaId", matriculaMaria,
                        "monto", 500.00,
                        "moneda", "PEN",
                        "metodoPago", "YAPE",
                        "referenciaExterna", "YAPE-987654",
                        "comprobante", "B001-000001",
                        "estado", "CONFIRMADO"
                ),
                "192.168.1.52",
                ahora.minusDays(3),
                "CORR-PAGO-YAPE-987654"
        );

        EventoAuditoria deudaPendienteJuan = evento(
                estudianteJuan,
                "pagos",
                "DEUDA_REGISTRADA",
                "Deuda",
                matriculaJuan,
                "CREAR",
                map(
                        "estudianteId", estudianteJuan,
                        "matriculaId", matriculaJuan,
                        "concepto", "MATRICULA",
                        "monto", 350.00,
                        "saldoPendiente", 350.00,
                        "estado", "PENDIENTE"
                ),
                "192.168.1.48",
                ahora.minusDays(2).minusHours(8),
                "CORR-DEUDA-EST001"
        );

        EventoAuditoria notaJuan = evento(
                docenteMatematica,
                "academico",
                "NOTA_REGISTRADA",
                "Nota",
                estudianteJuan + ":" + matematica + ":" + periodo2026 + ":PARCIAL",
                "CREAR",
                map(
                        "estudianteId", estudianteJuan,
                        "cursoId", matematica,
                        "periodoAcademicoId", periodo2026,
                        "tipoEvaluacion", "PARCIAL",
                        "descripcion", "Evaluación primer bimestre",
                        "calificacion", 18.5,
                        "estado", "REGISTRADA"
                ),
                "10.10.101.31",
                ahora.minusDays(1),
                "CORR-NOTA-EST001-MAT"
        );

        EventoAuditoria notaMaria = evento(
                docenteComunicacion,
                "academico",
                "NOTA_REGISTRADA",
                "Nota",
                estudianteMaria + ":" + comunicacion + ":" + periodo2026 + ":FINAL",
                "CREAR",
                map(
                        "estudianteId", estudianteMaria,
                        "cursoId", comunicacion,
                        "periodoAcademicoId", periodo2026,
                        "tipoEvaluacion", "FINAL",
                        "descripcion", "Evaluación final",
                        "calificacion", 17.0,
                        "estado", "REGISTRADA"
                ),
                "10.10.101.32",
                ahora.minusHours(12),
                "CORR-NOTA-EST002-COM"
        );

        EventoAuditoria asistenciaJuan = evento(
                profesor01,
                "academico",
                "ASISTENCIA_REGISTRADA",
                "Asistencia",
                estudianteJuan + ":" + matematica + ":1° A:" + periodo2026,
                "CREAR",
                map(
                        "estudianteId", estudianteJuan,
                        "cursoId", matematica,
                        "seccionId", "1° A",
                        "estado", "PRESENTE",
                        "observacion", "Asistencia normal"
                ),
                "10.10.101.31",
                ahora.minusHours(6),
                "CORR-ASIST-EST001-MAT"
        );

        EventoAuditoria tardanzaPedro = evento(
                docenteMatematica,
                "academico",
                "ASISTENCIA_TARDANZA",
                "Asistencia",
                estudiantePedro + ":" + ciencia + ":1° A Secundaria:" + periodo2026,
                "CREAR",
                map(
                        "estudianteId", estudiantePedro,
                        "cursoId", ciencia,
                        "seccionId", "1° A Secundaria",
                        "estado", "TARDANZA",
                        "observacion", "Ingreso tarde"
                ),
                "10.10.101.31",
                ahora.minusHours(2),
                "CORR-ASIST-EST003-CTA"
        );

        EventoAuditoria notificacionPago = evento(
                "SYSTEM",
                "notificaciones",
                "NOTIFICACION_ENVIADA",
                "Notificacion",
                "YAPE-987654",
                "CREAR",
                map(
                        "destinatarioId", estudianteMaria,
                        "tipo", "PAGO_CONFIRMADO",
                        "canal", "EMAIL",
                        "estado", "ENVIADA"
                ),
                "127.0.0.1",
                ahora.minusDays(3).plusMinutes(1),
                "CORR-PAGO-YAPE-987654"
        );

        eventoAuditoriaRepository.saveAll(List.of(
                matriculaCreadaJuan,
                matriculaCreadaMaria,
                matriculaPendientePedro,
                pagoConfirmadoMaria,
                deudaPendienteJuan,
                notaJuan,
                notaMaria,
                asistenciaJuan,
                tardanzaPedro,
                notificacionPago
        ));

        // =========================================================
        // 3. REPORTES GENERADOS
        // =========================================================
        ReporteGenerado reporteMatriculas = new ReporteGenerado(
                null,
                "MATRICULAS_PERIODO",
                admin,
                map(
                        "periodo", periodo2026,
                        "estados", List.of("CONFIRMADA", "PENDIENTE"),
                        "estudiantes", List.of(estudianteJuan, estudianteMaria, estudiantePedro)
                ),
                "PDF",
                "/reportes/2026/matriculas-periodo-2026.pdf",
                "LISTO",
                ahora.minusDays(2),
                ahora.minusDays(2).plusSeconds(20)
        );

        ReporteGenerado reportePagos = new ReporteGenerado(
                null,
                "PAGOS_MENSUAL",
                admin,
                map(
                        "periodo", "2026-08",
                        "moneda", "PEN",
                        "estudianteIds", List.of(estudianteJuan, estudianteMaria),
                        "referenciaConfirmada", "YAPE-987654"
                ),
                "EXCEL",
                "/reportes/2026/pagos-2026-08.xlsx",
                "LISTO",
                ahora.minusDays(1).minusHours(4),
                ahora.minusDays(1).minusHours(4).plusSeconds(15)
        );

        ReporteGenerado reporteNotas = new ReporteGenerado(
                null,
                "NOTAS_PERIODO",
                profesor01,
                map(
                        "periodo", periodo2026,
                        "cursos", List.of(matematica, comunicacion),
                        "estudiantes", List.of(estudianteJuan, estudianteMaria)
                ),
                "PDF",
                "/reportes/2026/notas-periodo-2026.pdf",
                "LISTO",
                ahora.minusHours(10),
                ahora.minusHours(10).plusSeconds(12)
        );

        ReporteGenerado reporteAsistencia = new ReporteGenerado(
                null,
                "ASISTENCIA_CURSO",
                profesor01,
                map(
                        "periodo", periodo2026,
                        "cursoId", ciencia,
                        "seccionId", "1° A Secundaria",
                        "estudianteId", estudiantePedro,
                        "estado", "TARDANZA"
                ),
                "CSV",
                "/reportes/2026/asistencia-cta-1a-secundaria.csv",
                "LISTO",
                ahora.minusHours(1),
                ahora.minusMinutes(55)
        );

        ReporteGenerado reporteAuditoria = new ReporteGenerado(
                null,
                "AUDITORIA_MICROSERVICIOS",
                admin,
                map(
                        "periodo", "2026-08",
                        "microservicios", List.of("matricula", "pagos", "academico", "notificaciones")
                ),
                "PDF",
                "/reportes/2026/auditoria-microservicios-2026-08.pdf",
                "GENERANDO",
                ahora.minusMinutes(10),
                null
        );

        ReporteGenerado reportePendiente = new ReporteGenerado(
                null,
                "DEUDAS_PENDIENTES",
                admin,
                map(
                        "periodo", "2026-08",
                        "estado", "PENDIENTE",
                        "estudianteId", estudianteJuan,
                        "matriculaId", matriculaJuan,
                        "montoPendiente", 350.00
                ),
                "EXCEL",
                null,
                "SOLICITADO",
                ahora.minusMinutes(2),
                null
        );

        reporteGeneradoRepository.saveAll(List.of(
                reporteMatriculas,
                reportePagos,
                reporteNotas,
                reporteAsistencia,
                reporteAuditoria,
                reportePendiente
        ));

        System.out.println(">>> Seeder reportes completado correctamente");
    }

    private EventoAuditoria evento(
            String usuarioId,
            String microservicio,
            String tipoEvento,
            String entidad,
            String entidadId,
            String accion,
            Map<String, Object> datos,
            String ip,
            LocalDateTime fecha,
            String correlationId
    ) {
        return new EventoAuditoria(
                null,
                usuarioId,
                microservicio,
                tipoEvento,
                entidad,
                entidadId,
                accion,
                datos,
                ip,
                fecha,
                correlationId
        );
    }

    private Map<String, Object> map(Object... valores) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        for (int i = 0; i < valores.length; i += 2) {
            resultado.put(String.valueOf(valores[i]), valores[i + 1]);
        }
        return resultado;
    }
}
