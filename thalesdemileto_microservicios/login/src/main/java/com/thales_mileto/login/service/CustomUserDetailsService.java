package com.thales_mileto.login.service;

import com.thales_mileto.login.model.Usuario;
import com.thales_mileto.login.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Se mantiene por compatibilidad, pero nunca asume que username sea único.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<Usuario> usuarios = usuarioRepository.findAllByUsername(username);
        Usuario usuario = usuarios.stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return construirUserDetails(usuario);
    }

    /**
     * Método usado por JWT. El ID sí es único y evita cualquier ambigüedad por usernames duplicados.
     */
    public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return construirUserDetails(usuario);
    }

    private UserDetails construirUserDetails(Usuario usuario) {
        return new User(
                // authentication.getName() queda como ID único del usuario.
                usuario.getId(),
                usuario.getPassword(),
                Boolean.TRUE.equals(usuario.getActivo()),
                true,
                true,
                true,
                usuario.getRoles() == null ? List.of() : usuario.getRoles().stream()
                        .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                        .collect(Collectors.toList())
        );
    }
}
