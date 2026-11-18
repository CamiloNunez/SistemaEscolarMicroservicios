package com.thales_mileto.login.dto;

import com.thales_mileto.login.model.TipoUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PerfilUsuarioRequest {

    @NotBlank
    private String usuarioId;

    @NotNull
    private TipoUsuario tipoUsuario;

    @NotBlank
    private String entidadId;

    private String codigoPerfil;

    /** Ejemplo: academico.docentes, matricula.apoderados, matricula.estudiantes */
    private String origen;

    private Boolean activo = true;
}
