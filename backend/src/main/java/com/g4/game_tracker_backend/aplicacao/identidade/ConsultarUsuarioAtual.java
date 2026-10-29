package com.g4.game_tracker_backend.aplicacao.identidade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

@Service
public class ConsultarUsuarioAtual {

    private final IdentidadeAutenticada identidade;
    private final UsuarioRepositorio usuarios;

    public ConsultarUsuarioAtual(IdentidadeAutenticada identidade, UsuarioRepositorio usuarios) {
        this.identidade = identidade;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public Usuario executar() {
        return usuarios.buscarPorId(identidade.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado"));
    }
}
