package com.g4.game_tracker_backend.dominio.marketplace.reputacao;

import java.util.UUID;

/** Evento de domínio: uma parte de um pedido avaliou a outra (útil para notificações). */
public record AvaliacaoMarketplaceCriada(UUID avaliacaoId, UUID pedidoId, UUID avaliadoId, int estrelas) {
}
