package com.thales_mileto.login.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "roles")
public class Rol {

    @Id
    private String id;

    @Indexed(unique = true)
    private String nombre; // ROLE_ADMIN, ROLE_PROFESOR, ROLE_ESTUDIANTE, ROLE_PADRE

    private String descripcion;

    private List<String> permisos;

    private Boolean activo = true;
}