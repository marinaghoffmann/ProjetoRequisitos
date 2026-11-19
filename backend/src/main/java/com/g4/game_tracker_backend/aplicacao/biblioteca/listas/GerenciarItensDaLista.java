package com.g4.game_tracker_backend.aplicacao.biblioteca.listas;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaPersonalizada;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

/**
 * F-04: o dono adiciona jogos ATIVOS do catálogo à lista, edita a nota explicativa de cada item e remove
 * itens. RN 2 (sem jogo repetido) e RN 3 (até 100 jogos) ficam no agregado.
 */
@CasoDeUso
public class GerenciarItensDaLista {

    private final ListaRepositorio listas;
    private final JogoRepositorio jogos;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public GerenciarItensDaLista(ListaRepositorio listas, JogoRepositorio jogos,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.listas = listas;
        this.jogos = jogos;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transacional
    public ListaPersonalizada adicionar(UUID listaId, UUID jogoId, String notaExplicativa) {
        UUID solicitante = verificadorDeAcesso.usuarioAtualId();
        ListaPersonalizada lista = ListasDoUsuario.existente(listas, listaId);
        lista.exigirDono(solicitante);
        exigirJogoAtivo(jogoId);
        return listas.salvar(lista.adicionarJogo(solicitante, jogoId, notaExplicativa, Instant.now()));
    }

    @Transacional
    public ListaPersonalizada editarNota(UUID listaId, UUID jogoId, String notaExplicativa) {
        ListaPersonalizada lista = ListasDoUsuario.existente(listas, listaId);
        return listas.salvar(lista.editarNotaExplicativa(verificadorDeAcesso.usuarioAtualId(), jogoId,
                notaExplicativa, Instant.now()));
    }

    @Transacional
    public ListaPersonalizada remover(UUID listaId, UUID jogoId) {
        ListaPersonalizada lista = ListasDoUsuario.existente(listas, listaId);
        return listas.salvar(lista.removerJogo(verificadorDeAcesso.usuarioAtualId(), jogoId, Instant.now()));
    }

    private void exigirJogoAtivo(UUID jogoId) {
        if (jogoId == null) {
            throw new RegraDeNegocioException("O jogo é obrigatório");
        }
        var jogo = jogos.buscarPorId(jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        if (jogo.status() != StatusCatalogo.ATIVO) {
            throw new RegraDeNegocioException("O jogo precisa estar ativo no catálogo");
        }
    }
}
