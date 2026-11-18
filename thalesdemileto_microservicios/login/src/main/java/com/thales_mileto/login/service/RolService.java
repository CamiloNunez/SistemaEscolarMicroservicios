package com.thales_mileto.login.service;

import com.thales_mileto.login.dto.RolDTO;
import com.thales_mileto.login.model.Rol;
import com.thales_mileto.login.repository.RolRepository;
import com.thales_mileto.login.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Rol> listar() {
        return rolRepository.findAll();
    }

    public Rol obtener(String id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
    }

    public Rol crear(RolDTO dto) {
        String nombre = normalizarNombre(dto.getNombre());
        if (rolRepository.existsByNombre(nombre)) {
            throw new RuntimeException("Ya existe un rol con el nombre " + nombre);
        }

        Rol rol = new Rol();
        rol.setNombre(nombre);
        rol.setDescripcion(dto.getDescripcion());
        rol.setPermisos(dto.getPermisos() == null ? Collections.emptyList() : dto.getPermisos());
        rol.setActivo(dto.getActivo() == null ? true : dto.getActivo());
        return rolRepository.save(rol);
    }

    public Rol actualizar(String id, RolDTO dto) {
        Rol rol = obtener(id);
        String nombre = normalizarNombre(dto.getNombre());

        boolean nombreOcupado = rolRepository.findAllByNombre(nombre).stream()
                .anyMatch(existente -> !existente.getId().equals(id));
        if (nombreOcupado) {
            throw new RuntimeException("Ya existe un rol con el nombre " + nombre);
        }

        rol.setNombre(nombre);
        rol.setDescripcion(dto.getDescripcion());
        rol.setPermisos(dto.getPermisos() == null ? Collections.emptyList() : dto.getPermisos());
        if (dto.getActivo() != null) rol.setActivo(dto.getActivo());
        return rolRepository.save(rol);
    }

    public void eliminar(String id) {
        Rol rol = obtener(id);
        if (usuarioRepository.existsByRolesId(id)) {
            throw new RuntimeException("No se puede eliminar el rol porque está asignado a uno o más usuarios");
        }
        rolRepository.delete(rol);
    }

    private String normalizarNombre(String valor) {
        String nombre = valor == null ? "" : valor.trim().toUpperCase();
        if (!nombre.startsWith("ROLE_")) nombre = "ROLE_" + nombre;
        return nombre;
    }
}
