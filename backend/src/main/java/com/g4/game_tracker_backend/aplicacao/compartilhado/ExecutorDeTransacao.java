package com.g4.game_tracker_backend.aplicacao.compartilhado;

import java.util.function.Supplier;

/**
 * Porta de transação dos casos de uso. A camada de aplicação demarca onde a transação começa e termina sem
 * depender do Spring; a infraestrutura implementa com o gerenciador de transações do JPA.
 *
 * <p>Uma {@link RuntimeException} lançada dentro do bloco desfaz tudo e chega a quem chamou sem alteração.
 * Se já existir uma transação aberta, o bloco participa dela.
 */
public interface ExecutorDeTransacao {

    /** Executa o bloco numa transação de escrita e devolve o resultado. */
    <T> T executar(Supplier<T> bloco);

    /** Executa o bloco numa transação de escrita. */
    void executarSemRetorno(Runnable bloco);

    /** Executa o bloco numa transação somente leitura. */
    <T> T consultar(Supplier<T> bloco);

    /**
     * Falha se não houver transação aberta. Para operações que só fazem sentido dentro da transação de
     * outro caso de uso (por exemplo, a reserva da negociação usada pelo checkout).
     */
    void exigirTransacaoAtiva();
}
