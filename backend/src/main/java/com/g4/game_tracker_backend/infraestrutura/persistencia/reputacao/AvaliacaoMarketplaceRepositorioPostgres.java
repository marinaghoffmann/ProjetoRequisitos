package com.g4.game_tracker_backend.infraestrutura.persistencia.reputacao;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplaceRepositorio;

@Repository
public class AvaliacaoMarketplaceRepositorioPostgres implements AvaliacaoMarketplaceRepositorio {

    private final AvaliacaoMarketplaceSpringDataRepository springData;

    public AvaliacaoMarketplaceRepositorioPostgres(AvaliacaoMarketplaceSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public AvaliacaoMarketplace salvar(AvaliacaoMarketplace avaliacao) {
        springData.save(AvaliacaoMarketplaceJpa.deDominio(avaliacao));
        return avaliacao;
    }

    @Override
    public boolean existePorPedidoEAvaliador(UUID pedidoId, UUID avaliadorId) {
        return springData.existsByPedidoIdAndAvaliadorId(pedidoId, avaliadorId);
    }

    @Override
    public int contarPorPedido(UUID pedidoId) {
        return springData.countByPedidoId(pedidoId);
    }

    @Override
    public List<AvaliacaoMarketplace> listarRecebidas(UUID avaliadoId) {
        return springData.findByAvaliadoIdOrderByCriadaEmDesc(avaliadoId).stream()
                .map(AvaliacaoMarketplaceJpa::paraDominio)
                .toList();
    }
}
