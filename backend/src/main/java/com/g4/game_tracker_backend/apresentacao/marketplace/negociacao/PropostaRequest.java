package com.g4.game_tracker_backend.apresentacao.marketplace.negociacao;

import java.math.BigDecimal;

import com.g4.game_tracker_backend.dominio.marketplace.negociacao.TipoProposta;

import jakarta.validation.constraints.NotNull;

/** valor só para VALOR; jogoOferecido só para TROCA; mensagem opcional, enviada junto no chat. */
public record PropostaRequest(
        @NotNull(message = "O tipo da proposta é obrigatório") TipoProposta tipo,
        BigDecimal valor,
        String jogoOferecido,
        String mensagem) {
}
