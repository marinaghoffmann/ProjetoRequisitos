package com.g4.game_tracker_backend.apresentacao.marketplace.pedidos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.ConsultarPedidos.PedidoDoUsuario;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.EnderecoEntrega;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.TransacaoPagamento;

/**
 * papel é COMPRA ou VENDA do ponto de vista de quem consulta. liberacaoAutomaticaEm só existe com o pedido
 * enviado (RN 7). O endereço vai para as duas partes, porque o vendedor precisa dele para enviar.
 */
public record PedidoResponse(UUID id, String papel, String origem, UUID anuncioId, String tituloAnuncio,
        UUID propostaId, String jogoOferecidoEmTroca, UUID compradorId, UUID vendedorId, UUID outraParteId,
        String nomeOutraParte, BigDecimal valorItem, String frete, BigDecimal valorFrete, BigDecimal total,
        EnderecoEntrega endereco, String formaPagamento, String finalCartao, String statusPagamento, String status,
        String codigoRastreio, Instant criadoEm, Instant prazoEnvio, Instant enviadoEm, Instant liberacaoAutomaticaEm,
        Instant concluidoEm, Instant canceladoEm) {

    static PedidoResponse de(PedidoDoUsuario visao) {
        PedidoMarketplace pedido = visao.pedido();
        TransacaoPagamento pagamento = pedido.pagamento();
        return new PedidoResponse(pedido.id(), visao.papel(), pedido.origem().name(), pedido.anuncioId(),
                pedido.tituloAnuncio(), pedido.propostaId(), pedido.jogoOferecidoEmTroca(), pedido.compradorId(),
                pedido.vendedorId(), visao.outraParteId(), visao.nomeOutraParte(), pedido.valorItem(),
                pedido.frete().name(), pedido.frete().valor(), pedido.total(), pedido.endereco(),
                pagamento.forma().name(), pagamento.finalCartao(), pagamento.status().name(), pedido.status().name(),
                pedido.codigoRastreio(), pedido.criadoEm(), pedido.prazoEnvio(), pedido.enviadoEm(),
                pedido.enviadoEm() == null ? null : pedido.enviadoEm().plus(PedidoMarketplace.TOLERANCIA_APOS_ENVIO),
                pedido.concluidoEm(), pedido.canceladoEm());
    }
}
