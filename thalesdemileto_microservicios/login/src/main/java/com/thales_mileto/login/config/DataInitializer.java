package com.thales_mileto.login.config;

import com.thales_mileto.login.model.Rol;
import com.thales_mileto.login.model.TipoUsuario;
import com.thales_mileto.login.model.Usuario;
import com.thales_mileto.login.repository.RolRepository;
import com.thales_mileto.login.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            List<Rol> roles = crearRolesIniciales();
            crearUsuariosIniciales(roles);
        } catch (DataAccessException e) {
            System.out.println("MongoDB no disponible. Se omite la carga inicial.");
        } catch (Exception e) {
            System.out.println("No se pudo ejecutar DataInitializer: " + e.getMessage());
        }
    }

    private List<Rol> crearRolesIniciales() {
        if (rolRepository.count() == 0) {
            List<Rol> roles = Arrays.asList(
                    new Rol(null, "ROLE_ADMIN", "Administrador del sistema",
                            Arrays.asList("READ", "WRITE", "DELETE", "UPDATE"), true),
                    new Rol(null, "ROLE_DIRECTOR", "Director del colegio",
                            Arrays.asList("READ", "WRITE", "UPDATE"), true),
                    new Rol(null, "ROLE_COORDINADOR", "Coordinador académico",
                            Arrays.asList("READ", "WRITE"), true),
                    new Rol(null, "ROLE_PROFESOR", "Profesor",
                            Arrays.asList("READ", "WRITE"), true),
                    new Rol(null, "ROLE_ESTUDIANTE", "Estudiante",
                            Arrays.asList("READ"), true),
                    new Rol(null, "ROLE_PADRE", "Padre de familia",
                            Arrays.asList("READ"), true)
            );

            rolRepository.saveAll(roles);
            System.out.println("Roles iniciales creados exitosamente");
        } else {
            System.out.println("Roles iniciales ya existen. No se insertaron duplicados.");
        }

        return rolRepository.findAll();
    }

    private void crearUsuariosIniciales(List<Rol> roles) {
        Rol rolProfesor = buscarRol(roles, "ROLE_PROFESOR");
        Rol rolAdmin = buscarRol(roles, "ROLE_ADMIN");

        // Los docentes sembrados en academico (DOC001, DOC002) se vinculan a estos usuarios.
        crearProfesor(rolProfesor, "profesor01", "Profesor Demo", "00000001", "999999001");
        crearProfesor(rolProfesor, "profesor02", "Profesora Demo Comunicación", "00000003", "999999003");

        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setEmail("admin@colegio.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setCedula("00000002");
            admin.setTelefono("999999002");
            admin.setActivo(true);
            admin.setRoles(Arrays.asList(rolAdmin));
            admin.setFechaCreacion(LocalDateTime.now());
            admin.setTipoUsuario(TipoUsuario.ADMINISTRATIVO);

            usuarioRepository.save(admin);
            System.out.println("Usuario creado: admin / admin123");
        } else {
            System.out.println("Usuario admin ya existe.");
        }
    }

    private void crearProfesor(Rol rolProfesor, String username, String nombre, String cedula, String telefono) {
        if (usuarioRepository.existsByUsername(username)) {
            System.out.println("Usuario " + username + " ya existe.");
            return;
        }
        Usuario profesor = new Usuario();
        profesor.setUsername(username);
        profesor.setEmail(username + "@colegio.com");
        profesor.setPassword(passwordEncoder.encode("password123"));
        profesor.setNombreCompleto(nombre);
        profesor.setCedula(cedula);
        profesor.setTelefono(telefono);
        profesor.setActivo(true);
        profesor.setRoles(Arrays.asList(rolProfesor));
        profesor.setFechaCreacion(LocalDateTime.now());
        profesor.setTipoUsuario(TipoUsuario.PROFESOR);

        usuarioRepository.save(profesor);
        System.out.println("Usuario creado: " + username + " / password123");
    }

    private Rol buscarRol(List<Rol> roles, String nombre) {
        return roles.stream()
                .filter(rol -> nombre.equals(rol.getNombre()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No existe el rol: " + nombre));
    }
}