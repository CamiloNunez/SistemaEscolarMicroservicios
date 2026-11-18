package com.thales_mileto.login.dto;

import com.thales_mileto.login.model.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UsuarioDTO {

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    // Obligatoria al crear. En actualización puede omitirse para conservar la actual.
    private String password;

    @NotBlank
    private String nombreCompleto;

    private String cedula;
    private String telefono;
    private TipoUsuario tipoUsuario;
    private List<String> roles;
    private String grado;
    private String seccion;
    private Boolean activo;
}
