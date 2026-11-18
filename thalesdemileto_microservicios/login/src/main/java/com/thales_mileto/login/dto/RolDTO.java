package com.thales_mileto.login.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RolDTO {
    @NotBlank
    private String nombre;
    private String descripcion;
    private List<String> permisos;
    private Boolean activo;
}
