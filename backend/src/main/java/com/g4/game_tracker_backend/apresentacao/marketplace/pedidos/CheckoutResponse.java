package com.g4.game_tracker_backend.apresentacao.marketplace.pedidos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.PrepararCheckout.ResumoCheckout;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.OpcaoFrete;

/** O que os dois passos do checkout mostram: o item, as opções de frete (RN 4) e o saldo da carteira. */
public record CheckoutResponse(String origem, UUID anuncioId, UUID propostaId, String tituloAnuncio,
        String fotoAnuncio, BigDecimal valorItem, String jogoOferecidoEmTroca, Instant prazoParaPagar,
        BigDecimal saldoCarteira, List<OpcaoFreteResponse> fretes) {

    static CheckoutResponse de(ResumoCheckout resumo) {
        return new CheckoutResponse(resumo.origem().name(), resumo.anuncioId(), resumo.propostaId(),
                resumo.tituloAnuncio(), resumo.fotoAnuncio(), resumo.valorItem(), resumo.jogoOferecidoEmTroca(),
                resumo.prazoParaPagar(), resumo.saldoCarteira(),
                Arrays.stream(OpcaoFrete.values()).map(OpcaoFreteResponse::de).toList());
    }

    public record OpcaoFreteResponse(String opcao, BigDecimal valor) {

        static OpcaoFreteResponse de(OpcaoFrete frete) {
            return new OpcaoFreteResponse(frete.name(), frete.valor());
        }
    }
}
