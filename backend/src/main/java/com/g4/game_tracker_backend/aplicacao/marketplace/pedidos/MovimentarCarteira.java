package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.marketplace.pedidos.Carteira;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CarteiraRepositorio;

/**
 * RN 6: a carteira é aberta na primeira transação do usuário, com o saldo inicial de simulação. Débitos e
 * créditos travam a linha da carteira até o fim da transação de quem chamou.
 */
@Component
class MovimentarCarteira {

    private final CarteiraRepositorio carteiras;

    MovimentarCarteira(CarteiraRepositorio carteiras) {
        this.carteiras = carteiras;
    }

    void debitar(UUID donoId, BigDecimal valor, Instant agora) {
        carteiras.salvar(paraAtualizar(donoId, agora).debitar(valor, agora));
    }

    void creditar(UUID donoId, BigDecimal valor, Instant agora) {
        carteiras.salvar(paraAtualizar(donoId, agora).creditar(valor, agora));
    }

    /** Quem ainda não movimentou a carteira vê o saldo inicial. */
    BigDecimal saldo(UUID donoId) {
        return carteiras.buscarPorDono(donoId).map(Carteira::saldo).orElse(Carteira.SALDO_INICIAL);
    }

    private Carteira paraAtualizar(UUID donoId, Instant agora) {
        return carteiras.buscarPorDonoParaAtualizar(donoId).orElseGet(() -> Carteira.abrir(donoId, agora));
    }
}
