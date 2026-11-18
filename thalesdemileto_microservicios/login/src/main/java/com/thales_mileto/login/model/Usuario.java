package com.thales_mileto.login.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String username;

    private String email;

    private String password;

    private String nombreCompleto;

    private String cedula;

    private String telefono;

    private Boolean activo = true;

    @DBRef
    private List<Rol> roles;

    private LocalDateTime fechaCreacion;

    private LocalDateTime ultimoAcceso;

    // Datos específicos del colegio
    private TipoUsuario tipoUsuario; // ESTUDIANTE, PROFESOR, ADMINISTRATIVO, PADRE

    private String grado; // Para estudiantes

    private String seccion; // Para estudiantes
}
