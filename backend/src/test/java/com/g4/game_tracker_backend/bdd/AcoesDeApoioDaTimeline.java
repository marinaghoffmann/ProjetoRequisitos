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

/**
 * Simula o recebimento de pedidos nos cenários do journal.
 * Também permite simular a falha de uma transação depois da execução de um caso de uso real.
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

    /**
     * Roda uma ação real (o caso de uso, que entra nesta transação) e quebra a transação logo depois dela,
     * já com o evento publicado: tudo sofre rollback, como numa falha de gravação.
     */
    void executarEFalharDepois(Runnable acao) {
        transacao.executeWithoutResult(status -> {
            acao.run();
            throw new FalhaSimulada("Falha simulada ao gravar a review");
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
