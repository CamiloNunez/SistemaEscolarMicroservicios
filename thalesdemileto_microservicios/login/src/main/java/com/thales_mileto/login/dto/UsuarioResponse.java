package com.thales_mileto.login.dto;

import com.thales_mileto.login.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class UsuarioResponse {
    private String id;
    private String username;
    private String email;
    private String nombreCompleto;
    private String cedula;
    private String telefono;
    private Boolean activo;
    private List<String> roles;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;
    private TipoUsuario tipoUsuario;
    private String grado;
    private String seccion;
    /** Vinculación con la entidad de dominio (docente, apoderado...). null si no está vinculado. */
    private PerfilUsuarioResponse perfil;
}
