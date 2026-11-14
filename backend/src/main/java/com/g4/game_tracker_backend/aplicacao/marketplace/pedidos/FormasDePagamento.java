package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.EstrategiaDePagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.FormaPagamento;

/** Escolhe a estratégia (padrão Strategy) pela forma de pagamento informada no checkout. */
@Component
class FormasDePagamento {

    private final Map<FormaPagamento, EstrategiaDePagamento> estrategias = new EnumMap<>(FormaPagamento.class);

    FormasDePagamento(List<EstrategiaDePagamento> todas) {
        todas.forEach(estrategia -> estrategias.put(estrategia.forma(), estrategia));
    }

    EstrategiaDePagamento para(FormaPagamento forma) {
        if (forma == null) {
            throw new RegraDeNegocioException("Escolha a forma de pagamento");
        }
        EstrategiaDePagamento estrategia = estrategias.get(forma);
        if (estrategia == null) {
            throw new IllegalStateException("Forma de pagamento sem estratégia: " + forma);
        }
        return estrategia;
    }
}
