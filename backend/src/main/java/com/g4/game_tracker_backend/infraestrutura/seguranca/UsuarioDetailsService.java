package com.g4.game_tracker_backend.infraestrutura.seguranca;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepositorio usuarios;

    public UsuarioDetailsService(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        return usuarios.buscarPorEmail(emailNormalizado)
                .map(usuario -> User.withUsername(usuario.email())
                        .password(usuario.senhaHash())
                        .authorities("ROLE_" + usuario.papel().name())
                        .disabled(!usuario.ativo())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
