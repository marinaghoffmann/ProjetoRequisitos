package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplaceRepositorio;

/** F-20 RN 8, RN 10 e RN 11: o vendedor registra o envio e o comprador confirma o recebimento. */
public class AcompanharPedido {

    private final PedidoMarketplaceRepositorio pedidos;
    private final ConcluirPedido concluirPedido;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final ExecutorDeTransacao transacao;

    public AcompanharPedido(PedidoMarketplaceRepositorio pedidos, ConcluirPedido concluirPedido,
            VerificadorDeAcesso verificadorDeAcesso,
            ExecutorDeTransacao transacao) {
        this.pedidos = pedidos;
        this.concluirPedido = concluirPedido;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.transacao = transacao;
    }

    public PedidoMarketplace registrarEnvio(UUID pedidoId, String codigoRastreio) {
        return transacao.executar(() -> {
            UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
            return pedidos.salvar(carregar(pedidoId).registrarEnvio(usuarioId, codigoRastreio, Instant.now()));
        });
    }

    public PedidoMarketplace confirmarRecebimento(UUID pedidoId) {
        return transacao.executar(() -> {
            UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
            Instant agora = Instant.now();
            return concluirPedido.registrar(carregar(pedidoId).confirmarRecebimento(usuarioId, agora), agora);
        });
    }

    private PedidoMarketplace carregar(UUID pedidoId) {
        return pedidos.buscarPorId(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
    }
}
