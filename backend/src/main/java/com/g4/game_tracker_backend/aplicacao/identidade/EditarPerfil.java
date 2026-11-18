package com.g4.game_tracker_backend.aplicacao.identidade;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

/** O usuário autenticado edita o próprio nome, bio e localização. */
@Service
public class EditarPerfil {

    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public EditarPerfil(UsuarioRepositorio usuarios, VerificadorDeAcesso verificadorDeAcesso) {
        this.usuarios = usuarios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public Usuario executar(String nome, String bio, String localizacao) {
        return executar(nome, bio, localizacao, null);
    }

    @Transactional
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
