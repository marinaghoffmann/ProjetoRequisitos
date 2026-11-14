package com.g4.game_tracker_backend.dominio.marketplace.pedidos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Padrão Strategy (GoF), o IFormaPagamentoStrategy do CML: cada forma de pagamento cobra e reembolsa do seu
 * jeito. Nenhuma chama um gateway real.
 */
public interface EstrategiaDePagamento {

    FormaPagamento forma();

    /** RN 5: valida e cobra o total; o pagamento aprovado volta EM_CUSTODIA (RN 7). */
    TransacaoPagamento cobrar(UUID compradorId, BigDecimal total, CartaoSimulado cartao, Instant agora);

    /** RN 9: devolve o valor ao comprador pela mesma forma em que ele pagou. */
    void reembolsar(UUID compradorId, TransacaoPagamento pagamento, Instant agora);
}
