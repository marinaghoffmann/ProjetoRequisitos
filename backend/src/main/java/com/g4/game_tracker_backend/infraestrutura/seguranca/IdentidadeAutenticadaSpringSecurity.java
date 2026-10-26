package com.g4.game_tracker_backend.infraestrutura.seguranca;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

@Component
public class IdentidadeAutenticadaSpringSecurity implements IdentidadeAutenticada {

    private final UsuarioRepositorio usuarios;

    public IdentidadeAutenticadaSpringSecurity(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    private JwtAuthenticationToken autenticacaoAtual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao instanceof JwtAuthenticationToken token && autenticacao.isAuthenticated()) {
            return token;
        }
        throw new AcessoNegadoException("É necessário estar autenticado");
    }

    @Override
    public UUID usuarioId() {
        return UUID.fromString(autenticacaoAtual().getToken().getSubject());
    }

    @Override
    public Papel papel() {
        return usuarioPersistidoAtual().papel();
    }

    private Usuario usuarioPersistidoAtual() {
        return usuarios.buscarPorId(usuarioId())
                .filter(Usuario::ativo)
                .orElseThrow(() -> new AcessoNegadoException("Usuário autenticado não está ativo"));
    }
}
