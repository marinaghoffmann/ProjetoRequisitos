package com.g4.game_tracker_backend.aplicacao.compartilhado;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Padrão Template Method: o esqueleto comum das rotinas agendadas que tratam prazos vencidos do marketplace
 * (reserva de 48 horas da F-19 RN 10, envio em 3 dias úteis da F-20 RN 9 e custódia de 7 dias da F-20 RN 7).
 *
 * <p>{@link #executar(Instant)} é final e fixa a ordem: busca os itens vencidos numa transação de leitura,
 * trata cada item na sua própria transação e registra a falha sem interromper os demais. As subclasses só
 * dizem quais itens venceram ({@link #vencidos}) e o que fazer com cada um ({@link #tratar}).
 *
 * <p>Recebe o instante como parâmetro para os testes não dependerem do relógio.
 *
 * @param <T> o agregado que tem o prazo (conversa, pedido)
 */
public abstract class RotinaDePrazo<T> {

    private static final Logger LOG = LoggerFactory.getLogger(RotinaDePrazo.class);

    private final ExecutorDeTransacao transacao;

    protected RotinaDePrazo(ExecutorDeTransacao transacao) {
        this.transacao = transacao;
    }

    /** Devolve quantos itens foram de fato alterados. */
    public final int executar(Instant agora) {
        List<T> vencidos = transacao.consultar(() -> vencidos(agora));
        int tratados = 0;
        for (T item : vencidos) {
            try {
                if (transacao.executar(() -> tratar(item, agora))) {
                    tratados++;
                }
            } catch (RuntimeException e) {
                LOG.error("Falha ao tratar o prazo de {}", descrever(item), e);
            }
        }
        return tratados;
    }

    /** Passo variável: os itens cujo prazo venceu até {@code agora}. */
    protected abstract List<T> vencidos(Instant agora);

    /** Passo variável: aplica a consequência do prazo vencido; devolve false se o item já não precisava. */
    protected abstract boolean tratar(T item, Instant agora);

    /** Passo variável: identifica o item no log de falha. */
    protected abstract String descrever(T item);
}
