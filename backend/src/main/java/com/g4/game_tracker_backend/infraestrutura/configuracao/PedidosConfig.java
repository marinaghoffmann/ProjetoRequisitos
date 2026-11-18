package com.g4.game_tracker_backend.infraestrutura.configuracao;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ReservasDaNegociacao;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.AcompanharPedido;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.CicloDoAnuncioNoPedido;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.ConcluirPedido;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.ConsultarPedidos;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.FormasDePagamento;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.GerarPedido;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.MovimentarCarteira;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.OrigemDoPedido;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.PagamentoCartao;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.PagamentoPix;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.PagamentoSaldoCarteira;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.PedidosParaReputacao;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.PrepararCheckout;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.ProcessarPrazosDosPedidos;
import com.g4.game_tracker_backend.dominio.compartilhado.PublicadorDeEventos;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CarteiraRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.EstrategiaDePagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplaceRepositorio;

/**
 * Wiring do contexto de pedidos (F-20). Os casos de uso são Java puro; aqui viram beans do Spring.
 *
 * <p>As estratégias de pagamento (padrão Strategy) são registradas aqui e chegam juntas, como lista, ao
 * {@link FormasDePagamento}. Uma forma nova de pagamento entra com um {@code @Bean} a mais.
 */
@Configuration
public class PedidosConfig {

    // ---- Colaboradores internos do checkout ----

    @Bean
    MovimentarCarteira movimentarCarteira(CarteiraRepositorio carteiras) {
        return new MovimentarCarteira(carteiras);
    }

    @Bean
    CicloDoAnuncioNoPedido cicloDoAnuncioNoPedido(AnuncioMarketplaceRepositorio anuncios,
            PublicadorDeEventos eventos) {
        return new CicloDoAnuncioNoPedido(anuncios, eventos);
    }

    @Bean
    ConcluirPedido concluirPedido(PedidoMarketplaceRepositorio pedidos, MovimentarCarteira carteira,
            CicloDoAnuncioNoPedido anuncios, PublicadorDeEventos eventos) {
        return new ConcluirPedido(pedidos, carteira, anuncios, eventos);
    }

    @Bean
    OrigemDoPedido origemDoPedido(CicloDoAnuncioNoPedido anuncios, ReservasDaNegociacao reservas,
            PedidoMarketplaceRepositorio pedidos,
            com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico moderacao) {
        return new OrigemDoPedido(anuncios, reservas, pedidos, moderacao);
    }

    // ---- Strategy: formas de pagamento (RN 5) ----

    @Bean
    PagamentoCartao pagamentoCartao() {
        return new PagamentoCartao();
    }

    @Bean
    PagamentoPix pagamentoPix() {
        return new PagamentoPix();
    }

    @Bean
    PagamentoSaldoCarteira pagamentoSaldoCarteira(MovimentarCarteira carteira) {
        return new PagamentoSaldoCarteira(carteira);
    }

    @Bean
    FormasDePagamento formasDePagamento(List<EstrategiaDePagamento> estrategias) {
        return new FormasDePagamento(estrategias);
    }

    // ---- Casos de uso ----

    @Bean
    PrepararCheckout prepararCheckout(OrigemDoPedido origens, MovimentarCarteira carteira,
            VerificadorDeAcesso verificadorDeAcesso, ExecutorDeTransacao transacao) {
        return new PrepararCheckout(origens, carteira, verificadorDeAcesso, transacao);
    }

    @Bean
    GerarPedido gerarPedido(OrigemDoPedido origens, FormasDePagamento formas, PedidoMarketplaceRepositorio pedidos,
            CicloDoAnuncioNoPedido anuncios, ReservasDaNegociacao reservas, VerificadorDeAcesso verificadorDeAcesso,
            ExecutorDeTransacao transacao) {
        return new GerarPedido(origens, formas, pedidos, anuncios, reservas, verificadorDeAcesso, transacao);
    }

    @Bean
    AcompanharPedido acompanharPedido(PedidoMarketplaceRepositorio pedidos, ConcluirPedido concluirPedido,
            VerificadorDeAcesso verificadorDeAcesso, ExecutorDeTransacao transacao) {
        return new AcompanharPedido(pedidos, concluirPedido, verificadorDeAcesso, transacao);
    }

    @Bean
    ConsultarPedidos consultarPedidos(PedidoMarketplaceRepositorio pedidos, MovimentarCarteira carteira,
            UsuarioRepositorio usuarios, VerificadorDeAcesso verificadorDeAcesso, ExecutorDeTransacao transacao) {
        return new ConsultarPedidos(pedidos, carteira, usuarios, verificadorDeAcesso, transacao);
    }

    @Bean
    ProcessarPrazosDosPedidos processarPrazosDosPedidos(PedidoMarketplaceRepositorio pedidos,
            FormasDePagamento formas, CicloDoAnuncioNoPedido anuncios, ConcluirPedido concluirPedido,
            ExecutorDeTransacao transacao) {
        return new ProcessarPrazosDosPedidos(pedidos, formas, anuncios, concluirPedido, transacao);
    }

    /** Porta da F-21 (reputação) sobre os pedidos concluídos. */
    @Bean
    PedidosParaReputacao pedidosParaReputacao(PedidoMarketplaceRepositorio pedidos) {
        return new PedidosParaReputacao(pedidos);
    }
}
