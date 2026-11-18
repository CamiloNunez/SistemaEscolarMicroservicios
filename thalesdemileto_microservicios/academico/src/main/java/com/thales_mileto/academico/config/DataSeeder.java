package com.thales_mileto.academico.config;

import com.thales_mileto.academico.client.LoginClient;
import com.thales_mileto.academico.model.*;
import com.thales_mileto.academico.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DocenteRepository docenteRepository;
    private final AsignacionDocenteRepository asignacionRepository;
    private final NotaRepository notaRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final PrerequisitoRepository prerequisitoRepository;
    private final LoginClient loginClient;

    /**
     * Docentes semilla y el usuario PROFESOR de login al que corresponde cada uno.
     * Los usuarios los crea el DataInitializer del microservicio login.
     */
    private static final Map<String, String> USUARIO_POR_CODIGO = new LinkedHashMap<>();
    static {
        USUARIO_POR_CODIGO.put("DOC001", "profesor01");
        USUARIO_POR_CODIGO.put("DOC002", "profesor02");
    }

    @Value("${seeder.login.username:admin}")
    private String seederUsername;

    @Value("${seeder.login.password:admin123}")
    private String seederPassword;


    @Override
    public void run(String... args) {

        if (docenteRepository.count() > 0) {
            System.out.println(">>> Seeder académico: ya existen datos, se revisan los vínculos de los docentes semilla");
            try {
                repararDocentesSemilla();
            } catch (RuntimeException e) {
                System.out.println(">>> No se pudieron revisar los docentes semilla: " + e.getMessage());
            }
            return;
        }

        // Los docentes deben apuntar a usuarios reales de login. Si login no responde
        // o faltan los usuarios, no se siembra nada y se reintenta en el próximo arranque.
        String auth;
        Map<String, String> usuarios;
        try {
            auth = loginClient.iniciarSesion(seederUsername, seederPassword);
            usuarios = resolverProfesores(auth);
        } catch (RuntimeException e) {
            System.out.println(">>> Seeder académico omitido: " + e.getMessage());
            return;
        }


        System.out.println(">>> Ejecutando Seeder académico...");


        /*
         * Los ids de usuario se obtienen de login (profesor01, profesor02).
         * Estudiantes y cursos vienen de matrícula y no se crean aquí.
         */

        String usuarioDocente1 = usuarios.get("DOC001");
        String usuarioDocente2 = usuarios.get("DOC002");


        String estudianteJuan = "EST001";
        String estudianteMaria = "EST002";
        String estudiantePedro = "EST003";


        String matematica = "MAT";
        String comunicacion = "COM";
        String ciencia = "CTA";


        String seccion1A = "1° A";
        String seccion2A = "2° A";
        String seccionSec = "1° A Secundaria";


        String periodo2026 = "2026";



        // ==============================
        // DOCENTES
        // ==============================


        Docente docenteMatematica = Docente.builder()
                .usuarioId(usuarioDocente1)
                .codigoDocente("DOC001")
                .especialidad("Matemática")
                .estado("ACTIVO")
                .fechaRegistro(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();



        Docente docenteComunicacion = Docente.builder()
                .usuarioId(usuarioDocente2)
                .codigoDocente("DOC002")
                .especialidad("Comunicación")
                .estado("ACTIVO")
                .fechaRegistro(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();



        docenteRepository.saveAll(
                List.of(
                        docenteMatematica,
                        docenteComunicacion
                )
        );

        // Registrar el perfil en login. Si falla, se deshace para no dejar
        // docentes sin vínculo (y el seeder se reintenta en el próximo arranque).
        try {
            for (Docente d : List.of(docenteMatematica, docenteComunicacion)) {
                loginClient.vincular(d.getUsuarioId(), d.getId(), d.getCodigoDocente(), auth);
            }
        } catch (RuntimeException e) {
            docenteRepository.deleteAll(List.of(docenteMatematica, docenteComunicacion));
            System.out.println(">>> Seeder académico revertido: " + e.getMessage());
            return;
        }



        // ==============================
        // ASIGNACIONES DOCENTES
        // ==============================


        AsignacionDocente asignacion1 =
                AsignacionDocente.builder()
                        .docenteId(docenteMatematica.getId())
                        .cursoId(matematica)
                        .seccionId(seccion1A)
                        .periodoAcademicoId(periodo2026)
                        .estado("ACTIVA")
                        .fechaAsignacion(LocalDateTime.now())
                        .build();



        AsignacionDocente asignacion2 =
                AsignacionDocente.builder()
                        .docenteId(docenteComunicacion.getId())
                        .cursoId(comunicacion)
                        .seccionId(seccion1A)
                        .periodoAcademicoId(periodo2026)
                        .estado("ACTIVA")
                        .fechaAsignacion(LocalDateTime.now())
                        .build();



        asignacionRepository.saveAll(
                List.of(
                        asignacion1,
                        asignacion2
                )
        );




        // ==============================
        // NOTAS
        // ==============================


        Nota notaJuan = Nota.builder()
                .estudianteId(estudianteJuan)
                .cursoId(matematica)
                .periodoAcademicoId(periodo2026)
                .docenteId(docenteMatematica.getId())
                .tipoEvaluacion("PARCIAL")
                .descripcion("Evaluación primer bimestre")
                .calificacion(18.5)
                .fechaEvaluacion(LocalDateTime.now())
                .estado("REGISTRADA")
                .fechaRegistro(LocalDateTime.now())
                .build();



        Nota notaMaria = Nota.builder()
                .estudianteId(estudianteMaria)
                .cursoId(comunicacion)
                .periodoAcademicoId(periodo2026)
                .docenteId(docenteComunicacion.getId())
                .tipoEvaluacion("FINAL")
                .descripcion("Evaluación final")
                .calificacion(17.0)
                .fechaEvaluacion(LocalDateTime.now())
                .estado("REGISTRADA")
                .fechaRegistro(LocalDateTime.now())
                .build();



        notaRepository.saveAll(
                List.of(
                        notaJuan,
                        notaMaria
                )
        );




        // ==============================
        // ASISTENCIAS
        // ==============================


        Asistencia asistenciaJuan =
                Asistencia.builder()
                        .estudianteId(estudianteJuan)
                        .cursoId(matematica)
                        .docenteId(docenteMatematica.getId())
                        .seccionId(seccion1A)
                        .fecha(LocalDateTime.now())
                        .estado(EstadoAsistencia.PRESENTE)
                        .observacion("Asistencia normal")
                        .fechaRegistro(LocalDateTime.now())
                        .build();



        Asistencia asistenciaPedro =
                Asistencia.builder()
                        .estudianteId(estudiantePedro)
                        .cursoId(ciencia)
                        .docenteId(docenteMatematica.getId())
                        .seccionId(seccionSec)
                        .fecha(LocalDateTime.now())
                        .estado(EstadoAsistencia.TARDANZA)
                        .observacion("Ingreso tarde")
                        .fechaRegistro(LocalDateTime.now())
                        .build();



        asistenciaRepository.saveAll(
                List.of(
                        asistenciaJuan,
                        asistenciaPedro
                )
        );




        // ==============================
        // PREREQUISITO
        // ==============================


        Prerequisito requisito =
                Prerequisito.builder()
                        .cursoId(ciencia)
                        .cursoPrerequisitoId(matematica)
                        .tipo("OBLIGATORIO")
                        .estado("ACTIVO")
                        .build();


        prerequisitoRepository.save(requisito);



        System.out.println(
                ">>> Seeder académico completado correctamente"
        );

    }

    /** codigoDocente -> id real del usuario PROFESOR en login. */
    private Map<String, String> resolverProfesores(String auth) {
        List<Map<String, Object>> catalogo = loginClient.listarUsuarios(auth);
        Map<String, String> resultado = new LinkedHashMap<>();

        for (Map.Entry<String, String> e : USUARIO_POR_CODIGO.entrySet()) {
            String username = e.getValue();
            Map<String, Object> usuario = catalogo.stream()
                    .filter(u -> username.equals(u.get("username")))
                    .filter(u -> LoginClient.TIPO_PROFESOR.equals(u.get("tipoUsuario")))
                    .filter(u -> Boolean.TRUE.equals(u.get("activo")))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "no existe en login un usuario PROFESOR activo '" + username
                                    + "' para el docente " + e.getKey()));
            resultado.put(e.getKey(), usuario.get("id").toString());
        }
        return resultado;
    }

    /**
     * Corrige docentes semilla creados antes con ids ficticios (USR-DOC-001...).
     * Solo reemplaza el usuarioId si el actual no existe en login, para no pisar
     * reasignaciones hechas a mano. En todos los casos asegura el perfil en login.
     */
    private void repararDocentesSemilla() {
        String auth = loginClient.iniciarSesion(seederUsername, seederPassword);
        Map<String, String> usuarios = resolverProfesores(auth);

        for (Map.Entry<String, String> e : usuarios.entrySet()) {
            String codigo = e.getKey();
            String usuarioReal = e.getValue();

            Optional<Docente> encontrado = docenteRepository.findByCodigoDocente(codigo);
            if (encontrado.isEmpty()) {
                continue;
            }
            Docente docente = encontrado.get();

            try {
                if (!usuarioReal.equals(docente.getUsuarioId())) {
                    boolean actualExiste = docente.getUsuarioId() != null
                            && loginClient.obtenerUsuario(docente.getUsuarioId(), auth).isPresent();
                    if (actualExiste) {
                        // Reasignado a otro usuario real: se respeta.
                        continue;
                    }
                    Optional<Docente> ocupante = docenteRepository.findByUsuarioId(usuarioReal);
                    if (ocupante.isPresent()) {
                        System.out.println(">>> " + codigo + ": el usuario " + USUARIO_POR_CODIGO.get(codigo)
                                + " ya pertenece al docente " + ocupante.get().getCodigoDocente() + "; no se corrige");
                        continue;
                    }
                    System.out.println(">>> " + codigo + ": usuarioId " + docente.getUsuarioId()
                            + " no existe en login, se reemplaza por " + USUARIO_POR_CODIGO.get(codigo));
                    docente.setUsuarioId(usuarioReal);
                    docente.setFechaActualizacion(LocalDateTime.now());
                    docenteRepository.save(docente);
                }
                loginClient.vincular(usuarioReal, docente.getId(), docente.getCodigoDocente(), auth);
            } catch (RuntimeException ex) {
                System.out.println(">>> No se pudo vincular " + codigo + ": " + ex.getMessage());
            }
        }
    }
}