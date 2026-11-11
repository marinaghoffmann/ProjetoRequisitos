package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CartaoSimulado;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.EstrategiaDePagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.FormaPagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.TransacaoPagamento;

/** RN 5 e RN 6: o saldo da carteira precisa cobrir o total, que é debitado; o reembolso volta para a carteira. */
@Component
class PagamentoSaldoCarteira implements EstrategiaDePagamento {

    private final MovimentarCarteira carteira;

    PagamentoSaldoCarteira(MovimentarCarteira carteira) {
        this.carteira = carteira;
    }

    @Override
    public FormaPagamento forma() {
        return FormaPagamento.SALDO_CARTEIRA;
    }

    @Override
    public TransacaoPagamento cobrar(UUID compradorId, BigDecimal total, CartaoSimulado cartao, Instant agora) {
        carteira.debitar(compradorId, total, agora);
        return TransacaoPagamento.emCustodia(FormaPagamento.SALDO_CARTEIRA, total, null, agora);
    }

    @Override
    public void reembolsar(UUID compradorId, TransacaoPagamento pagamento, Instant agora) {
        carteira.creditar(compradorId, pagamento.valor(), agora);
    }
}
