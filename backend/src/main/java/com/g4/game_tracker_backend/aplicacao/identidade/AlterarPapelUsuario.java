package com.g4.game_tracker_backend.aplicacao.identidade;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

@CasoDeUso
public class AlterarPapelUsuario {

    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public AlterarPapelUsuario(UsuarioRepositorio usuarios, VerificadorDeAcesso verificadorDeAcesso) {
        this.usuarios = usuarios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transacional
    public Usuario executar(UUID usuarioId, Papel novoPapel) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        return usuarios.salvar(usuario.comPapel(novoPapel));
    }
}
