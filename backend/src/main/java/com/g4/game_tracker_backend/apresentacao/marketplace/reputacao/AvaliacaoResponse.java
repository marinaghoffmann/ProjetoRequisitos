package com.g4.game_tracker_backend.apresentacao.marketplace.reputacao;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplace;

public record AvaliacaoResponse(UUID id, UUID pedidoId, UUID avaliadorId, String nomeAvaliador, UUID avaliadoId,
        String papelAvaliado, int estrelas, List<String> tags, String comentario, Instant criadaEm) {

    static AvaliacaoResponse de(AvaliacaoMarketplace avaliacao, Map<UUID, String> nomes) {
        return new AvaliacaoResponse(avaliacao.id(), avaliacao.pedidoId(), avaliacao.avaliadorId(),
                nomes.get(avaliacao.avaliadorId()), avaliacao.avaliadoId(), avaliacao.papelAvaliado().name(),
                avaliacao.estrelas(), avaliacao.tags().stream().map(Enum::name).sorted().toList(),
                avaliacao.comentario(), avaliacao.criadaEm());
    }
}
