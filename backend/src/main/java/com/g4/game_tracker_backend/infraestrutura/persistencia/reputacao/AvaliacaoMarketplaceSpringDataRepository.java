package com.g4.game_tracker_backend.infraestrutura.persistencia.reputacao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoMarketplaceSpringDataRepository extends JpaRepository<AvaliacaoMarketplaceJpa, UUID> {

    boolean existsByPedidoIdAndAvaliadorId(UUID pedidoId, UUID avaliadorId);

    int countByPedidoId(UUID pedidoId);

    List<AvaliacaoMarketplaceJpa> findByAvaliadoIdOrderByCriadaEmDesc(UUID avaliadoId);
}
