package com.thales_mileto.login.dto;
import com.thales_mileto.login.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private String tipo = "Bearer";

    private String id;

    private String username;

    private String email;

    private String nombreCompleto;

    private TipoUsuario tipoUsuario;

    private List<String> roles;

    private String grado;

    private String seccion;

    // Perfil funcional asociado (docente, apoderado, estudiante, etc.)
    private PerfilUsuarioResponse perfil;
}