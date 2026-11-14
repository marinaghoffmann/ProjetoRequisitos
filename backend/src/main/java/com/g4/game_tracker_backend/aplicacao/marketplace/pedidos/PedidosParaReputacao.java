package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.StatusPedido;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.FonteDePedidos;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.PedidoAvaliavel;

/**
 * Liga a F-21 aos pedidos reais. A entrega é dada como confirmada na conclusão do pedido, pela confirmação do
 * comprador ou pela tolerância de 7 dias, e é dali que contam os 15 dias para avaliar.
 */
@Component
public class PedidosParaReputacao implements FonteDePedidos {

    private final PedidoMarketplaceRepositorio pedidos;

    public PedidosParaReputacao(PedidoMarketplaceRepositorio pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public Optional<PedidoAvaliavel> buscarPedido(UUID pedidoId) {
        return pedidos.buscarPorId(pedidoId).map(pedido -> new PedidoAvaliavel(pedido.id(), pedido.compradorId(),
                pedido.vendedorId(), pedido.status() == StatusPedido.CONCLUIDO, pedido.concluidoEm()));
    }
}
