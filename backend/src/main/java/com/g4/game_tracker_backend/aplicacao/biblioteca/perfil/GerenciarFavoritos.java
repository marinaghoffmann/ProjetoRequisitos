package com.g4.game_tracker_backend.aplicacao.biblioteca.perfil;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.biblioteca.perfil.PerfilFavoritosRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

@Service
public class GerenciarFavoritos {
    private final PerfilFavoritosRepositorio perfis;
    private final JogoRepositorio jogos;
    private final VerificadorDeAcesso acesso;

    public GerenciarFavoritos(PerfilFavoritosRepositorio perfis, JogoRepositorio jogos, VerificadorDeAcesso acesso) {
        this.perfis = perfis;
        this.jogos = jogos;
        this.acesso = acesso;
    }

    @Transactional
    public void adicionar(UUID jogoId, Integer slot) {
        var perfil = perfis.buscarParaAlterar(acesso.usuarioAtualId());
        var atualizado = perfil.adicionar(jogoId, slot);
        exigirJogoAtivo(jogoId);
        perfis.salvar(atualizado);
    }

    @Transactional
    public void mover(UUID jogoId, int slot) {
        var perfil = perfis.buscarParaAlterar(acesso.usuarioAtualId());
        perfis.salvar(perfil.mover(jogoId, slot));
    }

    @Transactional
    public void substituir(int slot, UUID jogoId) {
        var perfil = perfis.buscarParaAlterar(acesso.usuarioAtualId());
        var atualizado = perfil.substituir(slot, jogoId);
        exigirJogoAtivo(jogoId);
        perfis.salvar(atualizado);
    }

    @Transactional
    public void remover(UUID jogoId) {
        var perfil = perfis.buscarParaAlterar(acesso.usuarioAtualId());
        perfis.salvar(perfil.remover(jogoId));
    }

    private void exigirJogoAtivo(UUID jogoId) {
        var jogo = jogos.buscarPorId(jogoId).orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        if (jogo.status() != StatusCatalogo.ATIVO) {
            throw new RegraDeNegocioException("O jogo precisa estar ativo no catálogo");
        }
    }
}
