package com.g4.game_tracker_backend.bdd;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.g4.game_tracker_backend.dominio.journal.eventos.CompraConcluida;
import com.g4.game_tracker_backend.dominio.journal.eventos.ConclusaoExcluida;
import com.g4.game_tracker_backend.dominio.journal.eventos.JogoZerado;
import com.g4.game_tracker_backend.dominio.journal.eventos.ReviewPublicada;

/**
 * Faz o papel de F-02 (review), F-11 (conclusão) e F-20 (pedido) nos cenários da F-16 enquanto elas não
 * existem. Cada ação roda em uma transação de verdade e publica o evento dentro dela, como os casos de uso
 * reais vão fazer; a ação só conta como gravada se a transação confirmar.
 *
 * Quando a funcionalidade real chegar, troque o método correspondente pelo caso de uso dela nos steps
 * de PassosTimelineAutomatica e apague-o daqui.
 */
@Component
public class AcoesDeApoioDaTimeline {

    private final TransactionTemplate transacao;
    private final ApplicationEventPublisher eventos;
    private final Set<String> acoesGravadas = ConcurrentHashMap.newKeySet();

    public AcoesDeApoioDaTimeline(TransactionTemplate transacao, ApplicationEventPublisher eventos) {
        this.transacao = transacao;
        this.eventos = eventos;
    }

    /** Publica a review; com falhar = true a gravação quebra depois do evento e tudo sofre rollback. */
    String publicarReview(String reviewId, UUID autorId, UUID jogoId, Instant quando, boolean falhar) {
        transacao.executeWithoutResult(status -> {
            gravarAoConfirmar(reviewId);
            eventos.publishEvent(new ReviewPublicada(autorId, jogoId, reviewId, quando));
            if (falhar) {
                throw new FalhaSimulada("Falha simulada ao gravar a review");
            }
        });
        return reviewId;
    }

    String publicarReview(UUID autorId, UUID jogoId, Instant quando) {
        return publicarReview("review-" + UUID.randomUUID(), autorId, jogoId, quando, false);
    }

    String registrarConclusao(UUID jogadorId, UUID jogoId, Instant quando) {
        String registroId = "conclusao-" + UUID.randomUUID();
        transacao.executeWithoutResult(status -> {
            gravarAoConfirmar(registroId);
            eventos.publishEvent(new JogoZerado(jogadorId, jogoId, registroId, quando));
        });
        return registroId;
    }

    void excluirConclusao(String registroId) {
        transacao.executeWithoutResult(status -> {
            acoesGravadas.remove(registroId);
            eventos.publishEvent(new ConclusaoExcluida(registroId));
        });
    }

    String confirmarRecebimento(UUID compradorId, UUID jogoId, Instant quando) {
        String pedidoId = "pedido-" + UUID.randomUUID();
        transacao.executeWithoutResult(status -> {
            gravarAoConfirmar(pedidoId);
            eventos.publishEvent(new CompraConcluida(compradorId, jogoId, pedidoId, quando));
        });
        return pedidoId;
    }

    boolean gravada(String acaoId) {
        return acoesGravadas.contains(acaoId);
    }

    private void gravarAoConfirmar(String acaoId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                acoesGravadas.add(acaoId);
            }
        });
    }

    /** Erro de infraestrutura simulado (não é regra de negócio), lançado dentro da transação da ação. */
    static final class FalhaSimulada extends RuntimeException {
        FalhaSimulada(String mensagem) {
            super(mensagem);
        }
    }
}
