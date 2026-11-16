package com.g4.game_tracker_backend.apresentacao.descoberta.similaridade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.descoberta.similaridade.ConsultarSimilares.Similar;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeJogo;

/**
 * Um jogo similar visto a partir do jogo consultado. grau é o grau efetivo (já rebaixado, se for o caso);
 * grauBase é o calculado ou definido pelo administrador. meuFeedback vem null sem login ou sem feedback.
 */
public record SimilarResponse(UUID similaridadeId, UUID jogoId, String titulo, String capaUrl, BigDecimal grau,
        BigDecimal grauBase, String origem, boolean rebaixada, int totalUteis, int totalNaoRelevantes,
        String meuFeedback, Instant calculadaEm) {

    static SimilarResponse de(Similar similar) {
        SimilaridadeJogo s = similar.similaridade();
        return new SimilarResponse(s.id(), similar.jogoId(), similar.titulo(), similar.capaUrl(), s.grauRelevancia(),
                s.grauBase(), s.origem().name(), s.rebaixada(), s.totalFeedbacksUteis(),
                s.totalFeedbacksNegativos(), similar.meuFeedback() == null ? null : similar.meuFeedback().name(),
                s.calculadaEm());
    }
}
