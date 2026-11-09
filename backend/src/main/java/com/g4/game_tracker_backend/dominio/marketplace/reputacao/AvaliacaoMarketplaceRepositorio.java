package com.g4.game_tracker_backend.dominio.marketplace.reputacao;

import java.util.List;
import java.util.UUID;

public interface AvaliacaoMarketplaceRepositorio {
    AvaliacaoMarketplace salvar(AvaliacaoMarketplace avaliacao);
    boolean existePorPedidoEAvaliador(UUID pedidoId, UUID avaliadorId);
    int contarPorPedido(UUID pedidoId);
    /** Avaliações recebidas pelo usuário, da mais recente para a mais antiga. */
    List<AvaliacaoMarketplace> listarRecebidas(UUID avaliadoId);
}
