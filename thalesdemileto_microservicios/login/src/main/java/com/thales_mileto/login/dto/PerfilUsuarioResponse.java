package com.thales_mileto.login.dto;

import com.thales_mileto.login.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUsuarioResponse {
    private String id;
    private String usuarioId;
    private TipoUsuario tipoUsuario;
    private String entidadId;
    private String codigoPerfil;
    private String origen;
    private Boolean activo;
}
