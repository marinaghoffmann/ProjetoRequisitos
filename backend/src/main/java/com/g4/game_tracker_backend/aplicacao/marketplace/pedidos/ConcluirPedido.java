package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.journal.eventos.CompraConcluida;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplaceRepositorio;

/**
 * F-20 RN 7 e RN 11, na mesma transação: o valor sai da custódia para a carteira do vendedor, o anúncio passa
 * a VENDIDO (a F-17 recalcula as estatísticas) e a compra entra no journal do comprador (F-16).
 */
@Component
class ConcluirPedido {

    private final PedidoMarketplaceRepositorio pedidos;
    private final MovimentarCarteira carteira;
    private final CicloDoAnuncioNoPedido anuncios;
    private final ApplicationEventPublisher eventos;

    ConcluirPedido(PedidoMarketplaceRepositorio pedidos, MovimentarCarteira carteira, CicloDoAnuncioNoPedido anuncios,
            ApplicationEventPublisher eventos) {
        this.pedidos = pedidos;
        this.carteira = carteira;
        this.anuncios = anuncios;
        this.eventos = eventos;
    }

    /** Recebe o pedido já no status CONCLUIDO (confirmado pelo comprador ou pela tolerância). */
    PedidoMarketplace registrar(PedidoMarketplace concluido, Instant agora) {
        PedidoMarketplace salvo = pedidos.salvar(concluido);
        carteira.creditar(salvo.vendedorId(), salvo.total(), agora);
        anuncios.vender(salvo.anuncioId(), agora);
        eventos.publishEvent(new CompraConcluida(salvo.compradorId(), salvo.jogoId(), salvo.id().toString(), agora));
        return salvo;
    }
}
