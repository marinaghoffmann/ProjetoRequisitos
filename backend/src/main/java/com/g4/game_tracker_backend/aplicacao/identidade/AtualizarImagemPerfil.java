package com.g4.game_tracker_backend.aplicacao.identidade;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.ArmazenamentoDeImagem;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

/** O usuário autenticado troca a própria foto de perfil ou o banner. */
@Service
public class AtualizarImagemPerfil {

    public enum Tipo { AVATAR, BANNER }

    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final ArmazenamentoDeImagem armazenamento;

    public AtualizarImagemPerfil(UsuarioRepositorio usuarios, VerificadorDeAcesso verificadorDeAcesso,
            ArmazenamentoDeImagem armazenamento) {
        this.usuarios = usuarios;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.armazenamento = armazenamento;
    }

    @Transactional
    public Usuario executar(Tipo tipo, byte[] conteudo, String extensao) {
        UUID id = verificadorDeAcesso.usuarioAtualId();
        Usuario atual = usuarios.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        String url = armazenamento.salvar(tipo.name().toLowerCase(), conteudo, extensao);
        Usuario atualizado = tipo == Tipo.AVATAR ? atual.comAvatar(url) : atual.comBanner(url);
        usuarios.salvar(atualizado);
        return atualizado;
    }
}