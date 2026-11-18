package com.thales_mileto.matricula.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "planes_estudio", uniqueConstraints =
        @UniqueConstraint(name = "uk_plan_programa_grado_periodo",
                columnNames = {"programa_academico_id", "grado_id", "periodo_academico_id"}))
public class PlanEstudios {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "programa_academico_id")
    private String programaAcademicoId;
    @Column(name = "grado_id")
    private String gradoId;
    @Column(name = "periodo_academico_id")
    private String periodoAcademicoId;

    private String nombre;           // "Plan de Estudios Secundaria 3° - 2025"
    private Integer totalHorasSemanales;

    // Cursos que componen el plan (la relación curso-plan tiene sus propios atributos)
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "plan_estudios_cursos",
            joinColumns = @JoinColumn(name = "plan_estudios_id"))
    private List<CursoEnPlanEmbedded> cursos;

    // "ACTIVO" | "INACTIVO" | "BORRADOR"
    private String estado;

    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    @Embeddable
    public static class CursoEnPlanEmbedded {
        private String cursoId;
        private String nombreCurso;    // desnormalizado para evitar joins frecuentes
        private Integer horasSemanales;
        private Boolean esObligatorio;
        private Integer orden;
    }
}
