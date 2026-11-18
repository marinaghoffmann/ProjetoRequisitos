package com.g4.game_tracker_backend.infraestrutura.transacao;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;

/**
 * Adapter da porta de transação sobre o gerenciador de transações do Spring (JPA). Mantém a semântica do
 * {@code @Transactional} padrão: propagação REQUIRED e rollback em qualquer {@link RuntimeException}.
 */
@Component
public class ExecutorDeTransacaoSpring implements ExecutorDeTransacao {

    private final TransactionTemplate escrita;
    private final TransactionTemplate leitura;

    public ExecutorDeTransacaoSpring(PlatformTransactionManager gerenciador) {
        this.escrita = new TransactionTemplate(gerenciador);
        this.leitura = new TransactionTemplate(gerenciador);
        this.leitura.setReadOnly(true);
    }

    @Override
    public <T> T executar(Supplier<T> bloco) {
        return escrita.execute(status -> bloco.get());
    }

    @Override
    public void executarSemRetorno(Runnable bloco) {
        escrita.executeWithoutResult(status -> bloco.run());
    }

    @Override
    public <T> T consultar(Supplier<T> bloco) {
        return leitura.execute(status -> bloco.get());
    }

    /** Equivale ao {@code @Transactional(propagation = MANDATORY)}. */
    @Override
    public void exigirTransacaoAtiva() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalTransactionStateException("Esta operação precisa rodar dentro de uma transação aberta");
        }
    }
}
