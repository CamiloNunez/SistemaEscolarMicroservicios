package com.thales_mileto.matricula.config;

import com.thales_mileto.matricula.model.*;
import com.thales_mileto.matricula.model.embedded.DireccionEmbedded;
import com.thales_mileto.matricula.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProgramaAcademicoRepository programaRepository;
    private final GradoRepository gradoRepository;
    private final CursoRepository cursoRepository;
    private final PeriodoAcademicoRepository periodoRepository;
    private final EstudianteRepository estudianteRepository;
    private final ApoderadoRepository apoderadoRepository;
    private final SeccionRepository seccionRepository;
    private final VacanteRepository vacanteRepository;
    private final PlanEstudiosRepository planEstudiosRepository;
    private final MatriculaRepository matriculaRepository;
    private final DetalleMatriculaRepository detalleMatriculaRepository;
    private final DocumentoMatriculaRepository documentoMatriculaRepository;
    private final PagoMatriculaRepository pagoMatriculaRepository;

    @Override
    @Transactional // si falla a mitad de la carga, no quedan datos de prueba incompletos
    public void run(String... args) {

        if (programaRepository.count() > 0) {
            System.out.println(">>> Seeder omitido: ya existen datos");
            return;
        }

        System.out.println(">>> Ejecutando Seeder inicial...");

        // =========================================================
        // 1. PROGRAMAS ACADÉMICOS (3)
        // =========================================================
        ProgramaAcademico inicial = ProgramaAcademico.builder()
                .codigo("INICIAL")
                .nombre("Educación Inicial")
                .descripcion("Nivel inicial")
                .duracionAnios(3)
                .nivelEducativo("INICIAL")
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        ProgramaAcademico primaria = ProgramaAcademico.builder()
                .codigo("PRIMARIA")
                .nombre("Educación Primaria")
                .descripcion("Nivel primaria")
                .duracionAnios(6)
                .nivelEducativo("PRIMARIA")
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        ProgramaAcademico secundaria = ProgramaAcademico.builder()
                .codigo("SECUNDARIA")
                .nombre("Educación Secundaria")
                .descripcion("Nivel secundaria")
                .duracionAnios(5)
                .nivelEducativo("SECUNDARIA")
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        programaRepository.saveAll(List.of(inicial, primaria, secundaria));

        // =========================================================
        // 2. GRADOS (3)
        // =========================================================
        Grado grado1Primaria = Grado.builder()
                .programaAcademicoId(primaria.getId())
                .numero(1)
                .nombre("Primer Grado")
                .descripcion("Primer grado de primaria")
                .edadMinima(6)
                .edadMaxima(7)
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Grado grado2Primaria = Grado.builder()
                .programaAcademicoId(primaria.getId())
                .numero(2)
                .nombre("Segundo Grado")
                .descripcion("Segundo grado de primaria")
                .edadMinima(7)
                .edadMaxima(8)
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Grado grado1Secundaria = Grado.builder()
                .programaAcademicoId(secundaria.getId())
                .numero(1)
                .nombre("Primer Año de Secundaria")
                .descripcion("Primer año de secundaria")
                .edadMinima(12)
                .edadMaxima(13)
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        gradoRepository.saveAll(List.of(grado1Primaria, grado2Primaria, grado1Secundaria));

        // =========================================================
        // 3. CURSOS (3)
        // =========================================================
        Curso matematica = Curso.builder()
                .codigo("MAT")
                .nombre("Matemática")
                .descripcion("Curso de matemática básica")
                .area("CIENCIAS")
                .tipo("OBLIGATORIO")
                .horasSemanales(5)
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Curso comunicacion = Curso.builder()
                .codigo("COM")
                .nombre("Comunicación")
                .descripcion("Curso de comunicación integral")
                .area("LETRAS")
                .tipo("OBLIGATORIO")
                .horasSemanales(5)
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Curso cienciaTecnologia = Curso.builder()
                .codigo("CTA")
                .nombre("Ciencia y Tecnología")
                .descripcion("Curso de ciencia, tecnología y ambiente")
                .area("CIENCIAS")
                .tipo("OBLIGATORIO")
                .horasSemanales(4)
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        cursoRepository.saveAll(List.of(matematica, comunicacion, cienciaTecnologia));

        // =========================================================
        // 4. PERIODOS ACADÉMICOS (3)
        // =========================================================
        PeriodoAcademico periodo2025 = PeriodoAcademico.builder()
                .codigo("2025")
                .nombre("Año Escolar 2025")
                .tipo("ANUAL")
                .fechaInicio(LocalDate.of(2025, 3, 1))
                .fechaFin(LocalDate.of(2025, 12, 19))
                .inicioMatricula(LocalDate.of(2025, 1, 6))
                .finMatricula(LocalDate.of(2025, 2, 21))
                .inicioMatriculaExtemporanea(LocalDate.of(2025, 2, 22))
                .finMatriculaExtemporanea(LocalDate.of(2025, 3, 10))
                .estado("CERRADO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        PeriodoAcademico periodo2026 = PeriodoAcademico.builder()
                .codigo("2026")
                .nombre("Año Escolar 2026")
                .tipo("ANUAL")
                .fechaInicio(LocalDate.of(2026, 3, 1))
                .fechaFin(LocalDate.of(2026, 12, 18))
                .inicioMatricula(LocalDate.of(2026, 1, 5))
                .finMatricula(LocalDate.of(2026, 2, 20))
                .inicioMatriculaExtemporanea(LocalDate.of(2026, 2, 21))
                .finMatriculaExtemporanea(LocalDate.of(2026, 3, 10))
                .estado("MATRICULA")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        PeriodoAcademico periodo2027 = PeriodoAcademico.builder()
                .codigo("2027")
                .nombre("Año Escolar 2027")
                .tipo("ANUAL")
                .fechaInicio(LocalDate.of(2027, 3, 1))
                .fechaFin(LocalDate.of(2027, 12, 17))
                .inicioMatricula(LocalDate.of(2027, 1, 4))
                .finMatricula(LocalDate.of(2027, 2, 19))
                .inicioMatriculaExtemporanea(LocalDate.of(2027, 2, 20))
                .finMatriculaExtemporanea(LocalDate.of(2027, 3, 9))
                .estado("PLANIFICADO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        periodoRepository.saveAll(List.of(periodo2025, periodo2026, periodo2027));

        // =========================================================
        // 5. PLANES DE ESTUDIO (3)
        // =========================================================
        PlanEstudios.CursoEnPlanEmbedded matEnPlan = PlanEstudios.CursoEnPlanEmbedded.builder()
                .cursoId(matematica.getId())
                .nombreCurso(matematica.getNombre())
                .horasSemanales(matematica.getHorasSemanales())
                .esObligatorio(true)
                .orden(1)
                .build();

        PlanEstudios.CursoEnPlanEmbedded comEnPlan = PlanEstudios.CursoEnPlanEmbedded.builder()
                .cursoId(comunicacion.getId())
                .nombreCurso(comunicacion.getNombre())
                .horasSemanales(comunicacion.getHorasSemanales())
                .esObligatorio(true)
                .orden(2)
                .build();

        PlanEstudios.CursoEnPlanEmbedded ctaEnPlan = PlanEstudios.CursoEnPlanEmbedded.builder()
                .cursoId(cienciaTecnologia.getId())
                .nombreCurso(cienciaTecnologia.getNombre())
                .horasSemanales(cienciaTecnologia.getHorasSemanales())
                .esObligatorio(true)
                .orden(3)
                .build();

        PlanEstudios planGrado1Primaria = PlanEstudios.builder()
                .programaAcademicoId(primaria.getId())
                .gradoId(grado1Primaria.getId())
                .periodoAcademicoId(periodo2026.getId())
                .nombre("Plan de Estudios Primaria 1° - 2026")
                .totalHorasSemanales(matematica.getHorasSemanales() + comunicacion.getHorasSemanales())
                .cursos(List.of(matEnPlan, comEnPlan))
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        PlanEstudios planGrado2Primaria = PlanEstudios.builder()
                .programaAcademicoId(primaria.getId())
                .gradoId(grado2Primaria.getId())
                .periodoAcademicoId(periodo2026.getId())
                .nombre("Plan de Estudios Primaria 2° - 2026")
                .totalHorasSemanales(matematica.getHorasSemanales() + comunicacion.getHorasSemanales() + cienciaTecnologia.getHorasSemanales())
                .cursos(List.of(matEnPlan, comEnPlan, ctaEnPlan))
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        PlanEstudios planGrado1Secundaria = PlanEstudios.builder()
                .programaAcademicoId(secundaria.getId())
                .gradoId(grado1Secundaria.getId())
                .periodoAcademicoId(periodo2026.getId())
                .nombre("Plan de Estudios Secundaria 1° - 2026")
                .totalHorasSemanales(matematica.getHorasSemanales() + comunicacion.getHorasSemanales() + cienciaTecnologia.getHorasSemanales())
                .cursos(List.of(matEnPlan, comEnPlan, ctaEnPlan))
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        planEstudiosRepository.saveAll(List.of(planGrado1Primaria, planGrado2Primaria, planGrado1Secundaria));

        // =========================================================
        // 6. SECCIONES (4)
        // =========================================================
        Seccion seccion1A = Seccion.builder()
                .gradoId(grado1Primaria.getId())
                .periodoAcademicoId(periodo2026.getId())
                .letra("A")
                .nombre("1° A")
                .turno("MAÑANA")
                .capacidadMaxima(30)
                .aulaAsignada("Aula 101")
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Seccion seccion1B = Seccion.builder()
                .gradoId(grado1Primaria.getId())
                .periodoAcademicoId(periodo2026.getId())
                .letra("B")
                .nombre("1° B")
                .turno("TARDE")
                .capacidadMaxima(28)
                .aulaAsignada("Aula 102")
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Seccion seccion2A = Seccion.builder()
                .gradoId(grado2Primaria.getId())
                .periodoAcademicoId(periodo2026.getId())
                .letra("A")
                .nombre("2° A")
                .turno("MAÑANA")
                .capacidadMaxima(25)
                .aulaAsignada("Aula 103")
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Seccion seccionSec1A = Seccion.builder()
                .gradoId(grado1Secundaria.getId())
                .periodoAcademicoId(periodo2026.getId())
                .letra("A")
                .nombre("1° A Secundaria")
                .turno("MAÑANA")
                .capacidadMaxima(28)
                .aulaAsignada("Aula 201")
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        seccionRepository.saveAll(List.of(seccion1A, seccion1B, seccion2A, seccionSec1A));

        // =========================================================
        // 7. VACANTES (4)
        // =========================================================
        Vacante vacantes1A = Vacante.builder()
                .seccionId(seccion1A.getId())
                .periodoAcademicoId(periodo2026.getId())
                .gradoId(grado1Primaria.getId())
                .nombreSeccion(seccion1A.getNombre())
                .totalVacantes(30)
                .vacantesOcupadas(1)
                .vacantesReservadas(0)
                .estado("DISPONIBLE")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Vacante vacantes1B = Vacante.builder()
                .seccionId(seccion1B.getId())
                .periodoAcademicoId(periodo2026.getId())
                .gradoId(grado1Primaria.getId())
                .nombreSeccion(seccion1B.getNombre())
                .totalVacantes(28)
                .vacantesOcupadas(0)
                .vacantesReservadas(0)
                .estado("DISPONIBLE")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Vacante vacantes2A = Vacante.builder()
                .seccionId(seccion2A.getId())
                .periodoAcademicoId(periodo2026.getId())
                .gradoId(grado2Primaria.getId())
                .nombreSeccion(seccion2A.getNombre())
                .totalVacantes(25)
                .vacantesOcupadas(1)
                .vacantesReservadas(0)
                .estado("DISPONIBLE")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Vacante vacantesSec1A = Vacante.builder()
                .seccionId(seccionSec1A.getId())
                .periodoAcademicoId(periodo2026.getId())
                .gradoId(grado1Secundaria.getId())
                .nombreSeccion(seccionSec1A.getNombre())
                .totalVacantes(28)
                .vacantesOcupadas(0)
                .vacantesReservadas(1) // matrícula de Pedro en estado PENDIENTE
                .estado("DISPONIBLE")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        vacanteRepository.saveAll(List.of(vacantes1A, vacantes1B, vacantes2A, vacantesSec1A));

        // =========================================================
        // 8. APODERADOS (3)
        // =========================================================
        DireccionEmbedded direccionFamiliaQuispe = DireccionEmbedded.builder()
                .calle("Av. Ejército")
                .numero("456")
                .distrito("Yanahuara")
                .provincia("Arequipa")
                .departamento("Arequipa")
                .codigoPostal("04013")
                .referencia("Frente al parque")
                .build();

        DireccionEmbedded direccionFamiliaFernandez = DireccionEmbedded.builder()
                .calle("Calle Los Álamos")
                .numero("789")
                .distrito("Cayma")
                .provincia("Arequipa")
                .departamento("Arequipa")
                .codigoPostal("04018")
                .referencia("Cerca al mercado")
                .build();

        Apoderado padreQuispe = Apoderado.builder()
                .nombres("Carlos")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Mamani")
                .dni("70123456")
                .tipoRelacion("PADRE")
                .esApoderadoPrincipal(true)
                .telefonoCelular("987654321")
                .telefonoFijo("054221100")
                .correo("carlos.quispe@example.com")
                .direccion(direccionFamiliaQuispe)
                .ocupacion("Ingeniero")
                .centroTrabajo("Constructora ABC")
                .telefonoTrabajo("054223344")
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Apoderado madreQuispe = Apoderado.builder()
                .nombres("Rosa")
                .apellidoPaterno("Mamani")
                .apellidoMaterno("Torres")
                .dni("70654321")
                .tipoRelacion("MADRE")
                .esApoderadoPrincipal(false)
                .telefonoCelular("987123456")
                .correo("rosa.mamani@example.com")
                .direccion(direccionFamiliaQuispe)
                .ocupacion("Contadora")
                .centroTrabajo("Estudio Contable Torres")
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Apoderado tutorFernandez = Apoderado.builder()
                .nombres("Luis")
                .apellidoPaterno("Fernández")
                .apellidoMaterno("Rojas")
                .dni("71234567")
                .tipoRelacion("TUTOR_LEGAL")
                .esApoderadoPrincipal(true)
                .telefonoCelular("956789123")
                .correo("luis.fernandez@example.com")
                .direccion(direccionFamiliaFernandez)
                .ocupacion("Abogado")
                .centroTrabajo("Estudio Jurídico Fernández & Asociados")
                .telefonoTrabajo("054334455")
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        apoderadoRepository.saveAll(List.of(padreQuispe, madreQuispe, tutorFernandez));

        // =========================================================
        // 9. ESTUDIANTES (3)
        // =========================================================
        Estudiante juan = Estudiante.builder()
                .nombres("Juan")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Mamani")
                .dni("80123456")
                .codigoEstudiante("EST001")
                .fechaNacimiento(LocalDate.of(2019, 5, 10))
                .genero("M")
                .lugarNacimiento("Arequipa")
                .nacionalidad("PERUANA")
                .direccion(direccionFamiliaQuispe)
                .telefono("987654321")
                .correo("juan.quispe@example.com")
                .grupoSanguineo("O+")
                .condicionesEspeciales(List.of())
                .requiereAtencionEspecial(false)
                .apoderadoIds(List.of(padreQuispe.getId(), madreQuispe.getId()))
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Estudiante maria = Estudiante.builder()
                .nombres("María")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Mamani")
                .dni("80123457")
                .codigoEstudiante("EST002")
                .fechaNacimiento(LocalDate.of(2018, 8, 22))
                .genero("F")
                .lugarNacimiento("Arequipa")
                .nacionalidad("PERUANA")
                .direccion(direccionFamiliaQuispe)
                .telefono("987654321")
                .correo("maria.quispe@example.com")
                .grupoSanguineo("A+")
                .condicionesEspeciales(List.of())
                .requiereAtencionEspecial(false)
                .apoderadoIds(List.of(padreQuispe.getId(), madreQuispe.getId()))
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Estudiante pedro = Estudiante.builder()
                .nombres("Pedro")
                .apellidoPaterno("Fernández")
                .apellidoMaterno("Rojas")
                .dni("80123458")
                .codigoEstudiante("EST003")
                .fechaNacimiento(LocalDate.of(2013, 2, 14))
                .genero("M")
                .lugarNacimiento("Arequipa")
                .nacionalidad("PERUANA")
                .direccion(direccionFamiliaFernandez)
                .telefono("956789123")
                .correo("pedro.fernandez@example.com")
                .grupoSanguineo("B+")
                .condicionesEspeciales(List.of("TDAH"))
                .requiereAtencionEspecial(true)
                .apoderadoIds(List.of(tutorFernandez.getId()))
                .estado("ACTIVO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        estudianteRepository.saveAll(List.of(juan, maria, pedro));

        // =========================================================
        // 10. MATRÍCULAS (3)
        // =========================================================
        Matricula matriculaJuan = Matricula.builder()
                .estudianteId(juan.getCodigoEstudiante())
                .nombreEstudiante(juan.getNombres() + " " + juan.getApellidoPaterno() + " " + juan.getApellidoMaterno())
                .grado(grado1Primaria.getNombre())
                .seccion(seccion1A.getNombre())
                .periodo(periodo2026.getCodigo())
                .seccionId(seccion1A.getId())
                .periodoAcademicoId(periodo2026.getId())
                .estado("CONFIRMADA")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Matricula matriculaMaria = Matricula.builder()
                .estudianteId(maria.getCodigoEstudiante())
                .nombreEstudiante(maria.getNombres() + " " + maria.getApellidoPaterno() + " " + maria.getApellidoMaterno())
                .grado(grado2Primaria.getNombre())
                .seccion(seccion2A.getNombre())
                .periodo(periodo2026.getCodigo())
                .seccionId(seccion2A.getId())
                .periodoAcademicoId(periodo2026.getId())
                .estado("CONFIRMADA")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Matricula matriculaPedro = Matricula.builder()
                .estudianteId(pedro.getCodigoEstudiante())
                .nombreEstudiante(pedro.getNombres() + " " + pedro.getApellidoPaterno() + " " + pedro.getApellidoMaterno())
                .grado(grado1Secundaria.getNombre())
                .seccion(seccionSec1A.getNombre())
                .periodo(periodo2026.getCodigo())
                .seccionId(seccionSec1A.getId())
                .periodoAcademicoId(periodo2026.getId())
                .estado("PENDIENTE")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        matriculaRepository.saveAll(List.of(matriculaJuan, matriculaMaria, matriculaPedro));

        // =========================================================
        // 11. DETALLES DE MATRÍCULA (7)
        // =========================================================
        DetalleMatricula detJuanMat = DetalleMatricula.builder()
                .matriculaId(matriculaJuan.getId())
                .cursoId(matematica.getId())
                .nombreCurso(matematica.getNombre())
                .codigoCurso(matematica.getCodigo())
                .horasSemanales(matematica.getHorasSemanales())
                .tipoInscripcion("OBLIGATORIO")
                .estado("INSCRITO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        DetalleMatricula detJuanCom = DetalleMatricula.builder()
                .matriculaId(matriculaJuan.getId())
                .cursoId(comunicacion.getId())
                .nombreCurso(comunicacion.getNombre())
                .codigoCurso(comunicacion.getCodigo())
                .horasSemanales(comunicacion.getHorasSemanales())
                .tipoInscripcion("OBLIGATORIO")
                .estado("INSCRITO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        DetalleMatricula detMariaMat = DetalleMatricula.builder()
                .matriculaId(matriculaMaria.getId())
                .cursoId(matematica.getId())
                .nombreCurso(matematica.getNombre())
                .codigoCurso(matematica.getCodigo())
                .horasSemanales(matematica.getHorasSemanales())
                .tipoInscripcion("OBLIGATORIO")
                .estado("INSCRITO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        DetalleMatricula detMariaCta = DetalleMatricula.builder()
                .matriculaId(matriculaMaria.getId())
                .cursoId(cienciaTecnologia.getId())
                .nombreCurso(cienciaTecnologia.getNombre())
                .codigoCurso(cienciaTecnologia.getCodigo())
                .horasSemanales(cienciaTecnologia.getHorasSemanales())
                .tipoInscripcion("OBLIGATORIO")
                .estado("INSCRITO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        DetalleMatricula detPedroMat = DetalleMatricula.builder()
                .matriculaId(matriculaPedro.getId())
                .cursoId(matematica.getId())
                .nombreCurso(matematica.getNombre())
                .codigoCurso(matematica.getCodigo())
                .horasSemanales(matematica.getHorasSemanales())
                .tipoInscripcion("OBLIGATORIO")
                .estado("INSCRITO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        DetalleMatricula detPedroCom = DetalleMatricula.builder()
                .matriculaId(matriculaPedro.getId())
                .cursoId(comunicacion.getId())
                .nombreCurso(comunicacion.getNombre())
                .codigoCurso(comunicacion.getCodigo())
                .horasSemanales(comunicacion.getHorasSemanales())
                .tipoInscripcion("OBLIGATORIO")
                .estado("INSCRITO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        DetalleMatricula detPedroCta = DetalleMatricula.builder()
                .matriculaId(matriculaPedro.getId())
                .cursoId(cienciaTecnologia.getId())
                .nombreCurso(cienciaTecnologia.getNombre())
                .codigoCurso(cienciaTecnologia.getCodigo())
                .horasSemanales(cienciaTecnologia.getHorasSemanales())
                .tipoInscripcion("ELECTIVO")
                .estado("INSCRITO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        detalleMatriculaRepository.saveAll(List.of(
                detJuanMat, detJuanCom, detMariaMat, detMariaCta, detPedroMat, detPedroCom, detPedroCta
        ));

        // =========================================================
        // 12. DOCUMENTOS DE MATRÍCULA (6)
        // =========================================================
        DocumentoMatricula docJuanDni = DocumentoMatricula.builder()
                .matriculaId(matriculaJuan.getId())
                .tipoDocumento("DNI")
                .nombreArchivo("dni_juan_quispe.pdf")
                .rutaAlmacenamiento("/matriculas/2026/EST001/dni_juan_quispe.pdf")
                .urlAcceso("https://storage.example.com/matriculas/2026/EST001/dni_juan_quispe.pdf")
                .mimeType("application/pdf")
                .tamañoBytes(204800L)
                .fechaEmision(LocalDate.of(2019, 6, 1))
                .esObligatorio(true)
                .estado("VERIFICADO")
                .observaciones("Documento legible y vigente")
                .usuarioVerificacion("SEEDER")
                .fechaVerificacion(LocalDateTime.now())
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        DocumentoMatricula docJuanFoto = DocumentoMatricula.builder()
                .matriculaId(matriculaJuan.getId())
                .tipoDocumento("FOTO")
                .nombreArchivo("foto_juan_quispe.jpg")
                .rutaAlmacenamiento("/matriculas/2026/EST001/foto_juan_quispe.jpg")
                .urlAcceso("https://storage.example.com/matriculas/2026/EST001/foto_juan_quispe.jpg")
                .mimeType("image/jpeg")
                .tamañoBytes(102400L)
                .fechaEmision(LocalDate.of(2026, 1, 10))
                .esObligatorio(true)
                .estado("PENDIENTE")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        DocumentoMatricula docMariaDni = DocumentoMatricula.builder()
                .matriculaId(matriculaMaria.getId())
                .tipoDocumento("DNI")
                .nombreArchivo("dni_maria_quispe.pdf")
                .rutaAlmacenamiento("/matriculas/2026/EST002/dni_maria_quispe.pdf")
                .urlAcceso("https://storage.example.com/matriculas/2026/EST002/dni_maria_quispe.pdf")
                .mimeType("application/pdf")
                .tamañoBytes(198000L)
                .fechaEmision(LocalDate.of(2018, 9, 5))
                .esObligatorio(true)
                .estado("VERIFICADO")
                .usuarioVerificacion("SEEDER")
                .fechaVerificacion(LocalDateTime.now())
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        DocumentoMatricula docMariaLibreta = DocumentoMatricula.builder()
                .matriculaId(matriculaMaria.getId())
                .tipoDocumento("LIBRETA_NOTAS")
                .nombreArchivo("libreta_maria_quispe_2025.pdf")
                .rutaAlmacenamiento("/matriculas/2026/EST002/libreta_maria_quispe_2025.pdf")
                .urlAcceso("https://storage.example.com/matriculas/2026/EST002/libreta_maria_quispe_2025.pdf")
                .mimeType("application/pdf")
                .tamañoBytes(305000L)
                .fechaEmision(LocalDate.of(2025, 12, 20))
                .esObligatorio(true)
                .estado("ENTREGADO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        DocumentoMatricula docPedroDni = DocumentoMatricula.builder()
                .matriculaId(matriculaPedro.getId())
                .tipoDocumento("DNI")
                .nombreArchivo("dni_pedro_fernandez.pdf")
                .rutaAlmacenamiento("/matriculas/2026/EST003/dni_pedro_fernandez.pdf")
                .urlAcceso("https://storage.example.com/matriculas/2026/EST003/dni_pedro_fernandez.pdf")
                .mimeType("application/pdf")
                .tamañoBytes(210000L)
                .fechaEmision(LocalDate.of(2013, 3, 1))
                .esObligatorio(true)
                .estado("PENDIENTE")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        DocumentoMatricula docPedroFichaSalud = DocumentoMatricula.builder()
                .matriculaId(matriculaPedro.getId())
                .tipoDocumento("FICHA_SALUD")
                .nombreArchivo("ficha_salud_pedro_fernandez.pdf")
                .rutaAlmacenamiento("/matriculas/2026/EST003/ficha_salud_pedro_fernandez.pdf")
                .urlAcceso("https://storage.example.com/matriculas/2026/EST003/ficha_salud_pedro_fernandez.pdf")
                .mimeType("application/pdf")
                .tamañoBytes(150000L)
                .fechaEmision(LocalDate.of(2026, 1, 15))
                .esObligatorio(true)
                .estado("RECHAZADO")
                .observaciones("Falta firma del médico tratante")
                .usuarioVerificacion("SEEDER")
                .fechaVerificacion(LocalDateTime.now())
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        documentoMatriculaRepository.saveAll(List.of(
                docJuanDni, docJuanFoto, docMariaDni, docMariaLibreta, docPedroDni, docPedroFichaSalud
        ));

        // =========================================================
        // 13. PAGOS DE MATRÍCULA (6)
        // =========================================================
        PagoMatricula pagoJuanMatricula = PagoMatricula.builder()
                .matriculaId(matriculaJuan.getId())
                .numeroComprobante("B001-000123")
                .concepto("DERECHO_MATRICULA")
                .monto(new BigDecimal("350.00"))
                .moneda("PEN")
                .metodoPago("YAPE")
                .fechaVencimiento(LocalDate.of(2026, 2, 20))
                .fechaPago(LocalDate.of(2026, 1, 15))
                .referenciaBancaria("YP-88213311")
                .entidadBancaria("BCP")
                .estado("PAGADO")
                .observaciones("Pago realizado a tiempo")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        PagoMatricula pagoJuanPension = PagoMatricula.builder()
                .matriculaId(matriculaJuan.getId())
                .numeroComprobante("B001-000124")
                .concepto("PENSION")
                .monto(new BigDecimal("280.00"))
                .moneda("PEN")
                .metodoPago("TRANSFERENCIA")
                .fechaVencimiento(LocalDate.of(2026, 3, 5))
                .entidadBancaria("BCP")
                .estado("PENDIENTE")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        PagoMatricula pagoMariaMatricula = PagoMatricula.builder()
                .matriculaId(matriculaMaria.getId())
                .numeroComprobante("B001-000125")
                .concepto("DERECHO_MATRICULA")
                .monto(new BigDecimal("350.00"))
                .moneda("PEN")
                .metodoPago("TARJETA")
                .fechaVencimiento(LocalDate.of(2026, 2, 20))
                .fechaPago(LocalDate.of(2026, 1, 18))
                .referenciaBancaria("TC-55210099")
                .entidadBancaria("Interbank")
                .estado("PAGADO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        PagoMatricula pagoMariaSeguro = PagoMatricula.builder()
                .matriculaId(matriculaMaria.getId())
                .numeroComprobante("B001-000126")
                .concepto("SEGURO")
                .monto(new BigDecimal("60.00"))
                .moneda("PEN")
                .metodoPago("EFECTIVO")
                .fechaVencimiento(LocalDate.of(2026, 3, 1))
                .fechaPago(LocalDate.of(2026, 1, 18))
                .estado("PAGADO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        PagoMatricula pagoPedroMatricula = PagoMatricula.builder()
                .matriculaId(matriculaPedro.getId())
                .numeroComprobante("B001-000127")
                .concepto("DERECHO_MATRICULA")
                .monto(new BigDecimal("420.00"))
                .moneda("PEN")
                .metodoPago("DEPOSITO")
                .fechaVencimiento(LocalDate.of(2026, 2, 20))
                .entidadBancaria("BBVA")
                .estado("ANULADO")
                .observaciones("Depósito no coincide con el monto esperado")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        PagoMatricula pagoPedroMateriales = PagoMatricula.builder()
                .matriculaId(matriculaPedro.getId())
                .numeroComprobante("B001-000128")
                .concepto("MATERIALES")
                .monto(new BigDecimal("150.00"))
                .moneda("PEN")
                .metodoPago("PLIN")
                .fechaVencimiento(LocalDate.of(2026, 3, 10))
                .fechaPago(LocalDate.of(2026, 2, 1))
                .referenciaBancaria("PL-77102233")
                .entidadBancaria("BCP")
                .estado("PAGADO")
                .usuarioRegistro("SEEDER")
                .fechaRegistro(LocalDateTime.now())
                .build();

        pagoMatriculaRepository.saveAll(List.of(
                pagoJuanMatricula, pagoJuanPension, pagoMariaMatricula, pagoMariaSeguro, pagoPedroMatricula, pagoPedroMateriales
        ));

        System.out.println(">>> Seeder completado correctamente: 13 tablas pobladas con al menos 3 registros cada una.");
    }
}