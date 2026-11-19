package com.g4.game_tracker_backend.aplicacao.identidade;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

/** O usuário autenticado edita o próprio nome, bio e localização. */
@CasoDeUso
public class EditarPerfil {

    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public EditarPerfil(UsuarioRepositorio usuarios, VerificadorDeAcesso verificadorDeAcesso) {
        this.usuarios = usuarios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transacional
    public Usuario executar(String nome, String bio, String localizacao) {
        return executar(nome, bio, localizacao, null);
    }

    @Transacional
    public Usuario executar(String nome, String bio, String localizacao, String bannerUrl) {
        UUID id = verificadorDeAcesso.usuarioAtualId();
        Usuario atual = usuarios.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        Usuario atualizado = atual.editarPerfil(nome, bio, localizacao);
        if (bannerUrl != null) {
            atualizado = atualizado.comBanner(bannerUrl);
        }
        usuarios.salvar(atualizado);
        return atualizado;
    }
}
