package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ReservasDaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CartaoSimulado;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.EnderecoEntrega;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.FormaPagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.OpcaoFrete;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.OrigemPedido;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.TransacaoPagamento;

/**
 * F-20, checkout: valida a origem (RN 1 e RN 2) e o endereço (RN 3), calcula o total (RN 4), cobra pela forma
 * escolhida (RN 5) e gera o pedido já pago, com o valor em custódia (RN 7). Tudo numa transação: se a cobrança
 * for recusada, nada é gravado.
 */
@Service
public class GerarPedido {

    private final OrigemDoPedido origens;
    private final FormasDePagamento formas;
    private final PedidoMarketplaceRepositorio pedidos;
    private final CicloDoAnuncioNoPedido anuncios;
    private final ReservasDaNegociacao reservas;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public GerarPedido(OrigemDoPedido origens, FormasDePagamento formas, PedidoMarketplaceRepositorio pedidos,
            CicloDoAnuncioNoPedido anuncios, ReservasDaNegociacao reservas, VerificadorDeAcesso verificadorDeAcesso) {
        this.origens = origens;
        this.formas = formas;
        this.pedidos = pedidos;
        this.anuncios = anuncios;
        this.reservas = reservas;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public PedidoMarketplace executar(UUID anuncioId, UUID propostaId, EnderecoEntrega endereco, OpcaoFrete frete,
            FormaPagamento forma, CartaoSimulado cartao) {
        UUID compradorId = verificadorDeAcesso.usuarioAtualId();
        Instant agora = Instant.now();
        OrigemDoPedido.Resolvida origem = origens.resolver(anuncioId, propostaId, compradorId, agora);
        AnuncioMarketplace anuncio = origem.anuncio();

        var estrategia = formas.para(forma);
        TransacaoPagamento pagamento = estrategia.cobrar(compradorId,
                PedidoMarketplace.calcularTotal(origem.valorItem(), frete), cartao, agora);
        PedidoMarketplace pedido = pedidos.salvar(PedidoMarketplace.gerar(origem.origem(), anuncio.id(),
                anuncio.jogoId(), anuncio.titulo(), origem.propostaId(), origem.jogoOferecidoEmTroca(), compradorId,
                anuncio.vendedorId(), origem.valorItem(), frete, endereco, pagamento, agora));

        if (origem.origem() == OrigemPedido.PROPOSTA) {
            reservas.vincularPedido(origem.propostaId(), pedido.id());
        } else {
            anuncios.reservar(anuncio, agora);
            reservas.recusarPropostasPendentes(anuncio.id(), agora);
        }
        return pedido;
    }
}
