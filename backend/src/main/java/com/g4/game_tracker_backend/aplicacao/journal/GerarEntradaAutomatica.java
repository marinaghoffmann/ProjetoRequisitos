package com.g4.game_tracker_backend.aplicacao.journal;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournalRepositorio;
import com.g4.game_tracker_backend.dominio.journal.TipoEventoPlataforma;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional.Propagacao;

/**
 * F-16: gera e remove as entradas automáticas do journal.
 *
 * RN 1: MANDATORY obriga a rodar dentro da transação da ação de origem. Se a ação falhar, a entrada
 * sofre rollback junto; se alguém publicar o evento fora de uma transação, a falha aparece na hora.
 * RN 2: excluir a ação de origem remove a entrada gerada por ela.
 */
@CasoDeUso
public class GerarEntradaAutomatica {

    private final EntradaDeJournalRepositorio entradas;
    private final JogoRepositorio jogos;

    public GerarEntradaAutomatica(EntradaDeJournalRepositorio entradas, JogoRepositorio jogos) {
        this.entradas = entradas;
        this.jogos = jogos;
    }

    /** Idempotente: o mesmo evento entregue duas vezes não duplica a entrada. */
    @Transacional(propagacao = Propagacao.EXIGIR_EXISTENTE)
    public EntradaDeJournal gerar(UUID donoId, UUID jogoId, TipoEventoPlataforma evento, String origemId,
            Instant ocorridoEm) {
        return entradas.buscarPorOrigem(evento, origemId).orElseGet(() -> entradas.salvar(
                EntradaDeJournal.criarAutomatica(donoId, jogoId, evento, origemId, texto(evento, jogoId),
                        ocorridoEm, Instant.now())));
    }

    @Transacional(propagacao = Propagacao.EXIGIR_EXISTENTE)
    public void sincronizarExclusao(TipoEventoPlataforma evento, String origemId) {
        entradas.buscarPorOrigem(evento, origemId).ifPresent(entrada -> entradas.remover(entrada.id()));
    }

    private String texto(TipoEventoPlataforma evento, UUID jogoId) {
        String titulo = jogos.buscarPorId(jogoId).map(Jogo::titulo).orElse("um jogo");
        return switch (evento) {
            case REVIEW_PUBLICADA -> "Publicou uma review de " + titulo;
            case JOGO_ZERADO -> "Zerou " + titulo;
            case COMPRA_CONCLUIDA -> "Comprou " + titulo + " no marketplace";
        };
    }
}
