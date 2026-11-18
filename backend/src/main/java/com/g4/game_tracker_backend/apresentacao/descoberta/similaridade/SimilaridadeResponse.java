package com.g4.game_tracker_backend.apresentacao.descoberta.similaridade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeJogo;

/** A conexão entre dois jogos, como gravada (par normalizado), com o grau efetivo e os totais de feedback. */
public record SimilaridadeResponse(UUID id, UUID jogoAId, UUID jogoBId, BigDecimal grau, BigDecimal grauBase,
        String origem, String status, boolean rebaixada, int totalUteis, int totalNaoRelevantes,
        Instant calculadaEm) {

    static SimilaridadeResponse de(SimilaridadeJogo s) {
        return new SimilaridadeResponse(s.id(), s.jogoAId(), s.jogoBId(), s.grauRelevancia(), s.grauBase(),
                s.origem().name(), s.status().name(), s.rebaixada(), s.totalFeedbacksUteis(),
                s.totalFeedbacksNegativos(), s.calculadaEm());
    }
}
