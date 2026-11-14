package com.g4.game_tracker_backend.dominio.marketplace.pedidos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Pagamento simulado do pedido (F-20 RN 5 e RN 7). Imutável. Nasce EM_CUSTODIA: o valor fica retido pela
 * plataforma, fora da carteira de qualquer um, até ser liberado ao vendedor ou reembolsado ao comprador.
 */
public record TransacaoPagamento(FormaPagamento forma, BigDecimal valor, StatusPagamento status, String finalCartao,
        Instant pagoEm, Instant liberadoEm, Instant reembolsadoEm) {

    public TransacaoPagamento {
        Objects.requireNonNull(forma, "A forma de pagamento é obrigatória");
        Objects.requireNonNull(valor, "O valor pago é obrigatório");
        Objects.requireNonNull(status, "O status do pagamento é obrigatório");
        Objects.requireNonNull(pagoEm, "A data do pagamento é obrigatória");
    }

    public static TransacaoPagamento emCustodia(FormaPagamento forma, BigDecimal valor, String finalCartao,
            Instant agora) {
        return new TransacaoPagamento(forma, valor, StatusPagamento.EM_CUSTODIA, finalCartao, agora, null, null);
    }

    TransacaoPagamento liberar(Instant agora) {
        exigirEmCustodia();
        return new TransacaoPagamento(forma, valor, StatusPagamento.LIBERADO, finalCartao, pagoEm, agora, null);
    }

    TransacaoPagamento reembolsar(Instant agora) {
        exigirEmCustodia();
        return new TransacaoPagamento(forma, valor, StatusPagamento.REEMBOLSADO, finalCartao, pagoEm, null, agora);
    }

    private void exigirEmCustodia() {
        if (status != StatusPagamento.EM_CUSTODIA) {
            throw new RegraDeNegocioException("O pagamento não está mais em custódia");
        }
    }
}
