package com.g4.game_tracker_backend.aplicacao.identidade;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

@Service
public class AlterarPapelUsuario {

    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public AlterarPapelUsuario(UsuarioRepositorio usuarios, VerificadorDeAcesso verificadorDeAcesso) {
        this.usuarios = usuarios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public Usuario executar(UUID usuarioId, Papel novoPapel) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        return usuarios.salvar(usuario.comPapel(novoPapel));
    }
}
