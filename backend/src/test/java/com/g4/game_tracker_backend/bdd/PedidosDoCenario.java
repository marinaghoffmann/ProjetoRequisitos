package com.g4.game_tracker_backend.bdd;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.AcompanharPedido;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.GerarPedido;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CartaoSimulado;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.EnderecoEntrega;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.FormaPagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.OpcaoFrete;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.StatusPedido;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.TransacaoPagamento;

import io.cucumber.spring.ScenarioScope;

/**
 * Pedidos reais da F-20 para os cenários da F-20 e da F-21: gerados pelo checkout, enviados e concluídos pelos
 * casos de uso. A passagem do tempo é simulada recuando as datas gravadas do pedido.
 */
@Component
@ScenarioScope
public class PedidosDoCenario {

    static final String CARTAO_VALIDO = "4111 1111 1111 1111";

    private final CenarioDoMarketplace cenario;
    private final GerarPedido gerarPedido;
    private final AcompanharPedido acompanharPedido;
    private final PedidoMarketplaceRepositorio pedidos;
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;

    private UUID pedidoAtual;

    public PedidosDoCenario(CenarioDoMarketplace cenario, GerarPedido gerarPedido, AcompanharPedido acompanharPedido,
            PedidoMarketplaceRepositorio pedidos, DadosDoCenario dados, ContextoDoCenario contexto) {
        this.cenario = cenario;
        this.gerarPedido = gerarPedido;
        this.acompanharPedido = acompanharPedido;
        this.pedidos = pedidos;
        this.dados = dados;
        this.contexto = contexto;
    }

    /** Anúncio novo do vendedor, comprado direto e levado até o status pedido pelos próprios casos de uso. */
    UUID criar(String comprador, String vendedor, BigDecimal valor, StatusPedido status, FormaPagamento forma) {
        cenario.anunciar(vendedor, StatusAnuncio.ATIVO, TipoNegociacao.AMBOS, valor);
        if (compraDireta(comprador, OpcaoFrete.ENVIO_SEGURO, forma) == null) {
            throw new IllegalStateException("O pedido de apoio foi recusado: " + contexto.erro().getMessage());
        }
        if (status == StatusPedido.ENVIADO || status == StatusPedido.CONCLUIDO) {
            dados.autenticar(vendedor);
            acompanharPedido.registrarEnvio(pedidoAtual, "BR123456789");
        }
        if (status == StatusPedido.CONCLUIDO) {
            dados.autenticar(comprador);
            acompanharPedido.confirmarRecebimento(pedidoAtual);
        }
        return pedidoAtual;
    }

    PedidoMarketplace compraDireta(String comprador, OpcaoFrete frete, FormaPagamento forma) {
        dados.autenticar(comprador);
        return registrar(contexto.obter(() -> gerarPedido.executar(cenario.anuncioId(), null, endereco("50000-000"),
                frete, forma, forma == FormaPagamento.CARTAO ? cartao(CARTAO_VALIDO) : null)));
    }

    PedidoMarketplace daProposta(String comprador, OpcaoFrete frete, FormaPagamento forma, EnderecoEntrega endereco,
            CartaoSimulado cartao) {
        dados.autenticar(comprador);
        return registrar(contexto.obter(() -> gerarPedido.executar(null, cenario.propostaId(), endereco, frete, forma,
                cartao)));
    }

    UUID pedidoId() {
        return pedidoAtual;
    }

    PedidoMarketplace pedido() {
        return pedidos.buscarPorId(pedidoAtual).orElseThrow();
    }

    /** Recua todas as datas gravadas do pedido, como se o tempo tivesse passado. */
    void recuar(Duration quanto) {
        PedidoMarketplace p = pedido();
        TransacaoPagamento pg = p.pagamento();
        pedidos.salvar(PedidoMarketplace.reconstituir(p.id(), p.origem(), p.anuncioId(), p.jogoId(), p.tituloAnuncio(),
                p.propostaId(), p.jogoOferecidoEmTroca(), p.compradorId(), p.vendedorId(), p.valorItem(), p.frete(),
                p.total(), p.endereco(),
                new TransacaoPagamento(pg.forma(), pg.valor(), pg.status(), pg.finalCartao(), pg.pagoEm().minus(quanto),
                        menos(pg.liberadoEm(), quanto), menos(pg.reembolsadoEm(), quanto)),
                p.status(), p.codigoRastreio(), p.criadoEm().minus(quanto), p.prazoEnvio().minus(quanto),
                menos(p.enviadoEm(), quanto), menos(p.concluidoEm(), quanto), menos(p.canceladoEm(), quanto)));
    }

    static EnderecoEntrega endereco(String cep) {
        return new EnderecoEntrega("João Silva", cep, "Rua da Aurora", "100", "Apto 12", "Recife", "PE");
    }

    static CartaoSimulado cartao(String numero) {
        return new CartaoSimulado(numero, "JOAO SILVA", "12/30", "123");
    }

    private PedidoMarketplace registrar(PedidoMarketplace pedido) {
        if (pedido != null) {
            pedidoAtual = pedido.id();
        }
        return pedido;
    }

    private static Instant menos(Instant instante, Duration quanto) {
        return instante == null ? null : instante.minus(quanto);
    }
}
