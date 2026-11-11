package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CartaoSimulado;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.EstrategiaDePagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.FormaPagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.TransacaoPagamento;

/** RN 5: PIX simulado, aprovado na confirmação do checkout. O reembolso é uma devolução simulada. */
@Component
class PagamentoPix implements EstrategiaDePagamento {

    @Override
    public FormaPagamento forma() {
        return FormaPagamento.PIX;
    }

    @Override
    public TransacaoPagamento cobrar(UUID compradorId, BigDecimal total, CartaoSimulado cartao, Instant agora) {
        return TransacaoPagamento.emCustodia(FormaPagamento.PIX, total, null, agora);
    }

    @Override
    public void reembolsar(UUID compradorId, TransacaoPagamento pagamento, Instant agora) {
        // Devolução simulada: nenhum banco é chamado; o status REEMBOLSADO no pedido é o registro.
    }
}
