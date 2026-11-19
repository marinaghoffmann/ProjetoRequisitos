package com.g4.game_tracker_backend.infraestrutura.persistencia.reputacao;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplaceRepositorio;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

@Repository
public class AvaliacaoMarketplaceRepositorioPostgres implements AvaliacaoMarketplaceRepositorio {

    private final AvaliacaoMarketplaceSpringDataRepository springData;
    private final EstadoPublico moderacao;

    public AvaliacaoMarketplaceRepositorioPostgres(AvaliacaoMarketplaceSpringDataRepository springData,
            EstadoPublico moderacao) {
        this.springData = springData;
        this.moderacao = moderacao;
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
                .filter(a -> moderacao.visivel(TipoConteudo.AVALIACAO_MARKETPLACE,a.id()))
                .toList();
    }
}
