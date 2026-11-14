package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplaceRepositorio;

/** F-20 RN 8, RN 10 e RN 11: o vendedor registra o envio e o comprador confirma o recebimento. */
@Service
public class AcompanharPedido {

    private final PedidoMarketplaceRepositorio pedidos;
    private final ConcluirPedido concluirPedido;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public AcompanharPedido(PedidoMarketplaceRepositorio pedidos, ConcluirPedido concluirPedido,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.pedidos = pedidos;
        this.concluirPedido = concluirPedido;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public PedidoMarketplace registrarEnvio(UUID pedidoId, String codigoRastreio) {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        return pedidos.salvar(carregar(pedidoId).registrarEnvio(usuarioId, codigoRastreio, Instant.now()));
    }

    @Transactional
    public PedidoMarketplace confirmarRecebimento(UUID pedidoId) {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        Instant agora = Instant.now();
        return concluirPedido.registrar(carregar(pedidoId).confirmarRecebimento(usuarioId, agora), agora);
    }

    private PedidoMarketplace carregar(UUID pedidoId) {
        return pedidos.buscarPorId(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
    }
}
