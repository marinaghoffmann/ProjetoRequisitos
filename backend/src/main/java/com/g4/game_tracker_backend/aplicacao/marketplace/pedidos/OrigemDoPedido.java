package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ReservasDaNegociacao;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ReservasDaNegociacao.ReservaAceita;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.OrigemPedido;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplaceRepositorio;

/**
 * F-20 RN 1 e RN 2, usadas pela prévia e pela geração do pedido: de onde ele vem, por quanto o item sai e se
 * o anúncio ainda está livre para um pedido.
 */
@Component
class OrigemDoPedido {

    private final CicloDoAnuncioNoPedido anuncios;
    private final ReservasDaNegociacao reservas;
    private final PedidoMarketplaceRepositorio pedidos;

    OrigemDoPedido(CicloDoAnuncioNoPedido anuncios, ReservasDaNegociacao reservas, PedidoMarketplaceRepositorio pedidos) {
        this.anuncios = anuncios;
        this.reservas = reservas;
        this.pedidos = pedidos;
    }

    /**
     * Exatamente um dos dois: a proposta aceita do comprador ou o anúncio da compra direta. O pedido ativo
     * (RN 2) é checado primeiro, para quem tenta pagar de novo saber que o pedido já existe.
     */
    Resolvida resolver(UUID anuncioId, UUID propostaId, UUID compradorId, Instant agora) {
        if ((anuncioId == null) == (propostaId == null)) {
            throw new RegraDeNegocioException("Informe o anúncio da compra direta ou a proposta aceita");
        }
        UUID anuncioDoPedido = propostaId != null ? reservas.anuncioDaProposta(propostaId) : anuncioId;
        if (pedidos.existeAtivoParaAnuncio(anuncioDoPedido)) {
            throw new RegraDeNegocioException("Este anúncio já tem um pedido em andamento");
        }
        return propostaId != null
                ? daProposta(propostaId, compradorId, agora)
                : daCompraDireta(anuncioId, compradorId);
    }

    /** Proposta ACEITA do próprio comprador, com o anúncio reservado (EM_NEGOCIACAO), pelo valor da proposta. */
    private Resolvida daProposta(UUID propostaId, UUID compradorId, Instant agora) {
        ReservaAceita reserva = reservas.reservaDoComprador(propostaId, compradorId, agora);
        AnuncioMarketplace anuncio = anuncios.carregar(reserva.anuncioId());
        if (anuncio.status() != StatusAnuncio.EM_NEGOCIACAO) {
            throw new RegraDeNegocioException("O anúncio não está reservado para esta proposta");
        }
        return new Resolvida(OrigemPedido.PROPOSTA, anuncio, propostaId, reserva.valorDoItem(),
                reserva.jogoOferecido(), reserva.prazoParaPagar());
    }

    /** Anúncio ATIVO de outra pessoa, pelo preço anunciado; anúncio só de troca não tem compra direta. */
    private Resolvida daCompraDireta(UUID anuncioId, UUID compradorId) {
        AnuncioMarketplace anuncio = anuncios.carregar(anuncioId);
        if (anuncio.pertenceA(compradorId)) {
            throw new RegraDeNegocioException("Você não pode comprar o próprio anúncio");
        }
        if (anuncio.status() != StatusAnuncio.ATIVO) {
            throw new RegraDeNegocioException("Só é possível comprar anúncios ativos");
        }
        if (anuncio.tipoNegociacao() == TipoNegociacao.TROCA) {
            throw new RegraDeNegocioException("Anúncios só de troca não aceitam compra direta");
        }
        return new Resolvida(OrigemPedido.COMPRA_DIRETA, anuncio, null, anuncio.preco(), null, null);
    }

    /** prazoParaPagar só existe na origem PROPOSTA (F-19 RN 10). */
    record Resolvida(OrigemPedido origem, AnuncioMarketplace anuncio, UUID propostaId, BigDecimal valorItem,
            String jogoOferecidoEmTroca, Instant prazoParaPagar) {
    }
}
