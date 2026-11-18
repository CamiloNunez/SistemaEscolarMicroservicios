package com.thales_mileto.login.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "perfiles_usuario")
public class PerfilUsuario {

    @Id
    private String id;

    /** Id del usuario autenticable de la colección usuarios. */
    @Indexed(unique = true)
    private String usuarioId;

    /** Tipo funcional del usuario dentro del colegio. */
    private TipoUsuario tipoUsuario;

    /**
     * Id del registro de dominio asociado.
     * Ejemplos: id de docente, apoderado o estudiante.
     */
    @Indexed
    private String entidadId;

    /** Código legible del perfil, por ejemplo DOC001, APO001 o EST001. */
    private String codigoPerfil;

    /** Microservicio/colección propietaria del registro de dominio. */
    private String origen;

    private Boolean activo = true;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
