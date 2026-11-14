package com.g4.game_tracker_backend.dominio.marketplace.reputacao;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta ACL para o contexto de pedidos (F-20), o IPedidoConcluidoGateway do CML. A F-20 implementa como
 * bean Spring; enquanto ela não existe, nenhum pedido pode ser avaliado.
 */
public interface FonteDePedidos {
    Optional<PedidoAvaliavel> buscarPedido(UUID pedidoId);
}
