package com.g4.game_tracker_backend.apresentacao.marketplace.negociacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.marketplace.negociacao.PropostaNegociacao;

/** prazoParaPagar só vem preenchido na proposta aceita (RN 10); motivo traz o aviso da recusa automática. */
public record PropostaResponse(UUID id, String tipo, BigDecimal valorOferecido, String jogoOferecido, String status,
        String motivo, UUID pedidoId, Instant enviadaEm, Instant respondidaEm, Instant prazoParaPagar) {

    static PropostaResponse de(PropostaNegociacao proposta) {
        return new PropostaResponse(proposta.id(), proposta.tipo().name(), proposta.valorOferecido(),
                proposta.jogoOferecido(), proposta.status().name(), proposta.motivo(), proposta.pedidoId(),
                proposta.enviadaEm(), proposta.respondidaEm(), proposta.prazoParaPagar());
    }
}
