package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CalendarioDiasUteis;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CartaoSimulado;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.EstrategiaDePagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.FormaPagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.TransacaoPagamento;

/** RN 5: cartão simulado; os dados válidos aprovam na hora. O reembolso é um estorno simulado no cartão. */
@Component
class PagamentoCartao implements EstrategiaDePagamento {

    @Override
    public FormaPagamento forma() {
        return FormaPagamento.CARTAO;
    }

    @Override
    public TransacaoPagamento cobrar(UUID compradorId, BigDecimal total, CartaoSimulado cartao, Instant agora) {
        if (cartao == null) {
            throw new RegraDeNegocioException("Informe os dados do cartão");
        }
        String finalCartao = cartao.validar(YearMonth.from(agora.atZone(CalendarioDiasUteis.FUSO)));
        return TransacaoPagamento.emCustodia(FormaPagamento.CARTAO, total, finalCartao, agora);
    }

    @Override
    public void reembolsar(UUID compradorId, TransacaoPagamento pagamento, Instant agora) {
        // Estorno simulado: nenhuma operadora é chamada; o status REEMBOLSADO no pedido é o registro.
    }
}
