package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.AcompanharPedido;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.ConsultarPedidos;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.ConsultarPedidos.PedidoDoUsuario;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.ProcessarPrazosDosPedidos;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogoRepositorio;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournalRepositorio;
import com.g4.game_tracker_backend.dominio.journal.TipoEventoPlataforma;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CartaoSimulado;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.Carteira;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CarteiraRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.FormaPagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.OpcaoFrete;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.StatusPagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.StatusPedido;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/**
 * Steps da F-20. O pedido é gerado pelo checkout real (compra direta ou proposta aceita na F-19), por meio de
 * PedidosDoCenario; as rotinas rodam com o instante atual sobre pedidos com as datas recuadas.
 */
public class PassosPedidoCheckout {

    private final CenarioDoMarketplace cenario;
    private final PedidosDoCenario pedidos;
    private final AcompanharPedido acompanharPedido;
    private final ConsultarPedidos consultarPedidos;
    private final ProcessarPrazosDosPedidos prazos;
    private final CarteiraRepositorio carteiras;
    private final EntradaDeJournalRepositorio entradas;
    private final EstatisticaJogoRepositorio estatisticas;
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;

    private List<PedidoDoUsuario> pedidosConsultados;

    public PassosPedidoCheckout(CenarioDoMarketplace cenario, PedidosDoCenario pedidos,
            AcompanharPedido acompanharPedido, ConsultarPedidos consultarPedidos, ProcessarPrazosDosPedidos prazos,
            CarteiraRepositorio carteiras, EntradaDeJournalRepositorio entradas,
            EstatisticaJogoRepositorio estatisticas, DadosDoCenario dados, ContextoDoCenario contexto) {
        this.cenario = cenario;
        this.pedidos = pedidos;
        this.acompanharPedido = acompanharPedido;
        this.consultarPedidos = consultarPedidos;
        this.prazos = prazos;
        this.carteiras = carteiras;
        this.entradas = entradas;
        this.estatisticas = estatisticas;
        this.dados = dados;
        this.contexto = contexto;
    }

    // ---- Origem do pedido (RN 1 e RN 2) ----

    @Dado("uma proposta aceita de R$ {reais} entre o comprador {string} e o vendedor {string}")
    public void propostaAceita(BigDecimal valor, String comprador, String vendedor) {
        cenario.conversaEntre(comprador, vendedor);
        cenario.proporValor(comprador, valor);
        cenario.aceitar(vendedor);
    }

    @Dado("uma proposta de troca pelo jogo {string} aceita entre o comprador {string} e o vendedor {string}")
    public void propostaDeTrocaAceita(String jogo, String comprador, String vendedor) {
        cenario.conversaEntre(comprador, vendedor);
        cenario.proporTroca(comprador, jogo);
        cenario.aceitar(vendedor);
    }

    @Dado("um anúncio {string} de R$ {reais} do vendedor {string}")
    public void anuncioComPreco(String status, BigDecimal preco, String vendedor) {
        cenario.anunciar(vendedor, StatusAnuncio.valueOf(status), TipoNegociacao.AMBOS, preco);
    }

    @Dado("um anúncio só de troca do vendedor {string}")
    public void anuncioSoDeTroca(String vendedor) {
        cenario.anunciar(vendedor, StatusAnuncio.ATIVO, TipoNegociacao.TROCA, CenarioDoMarketplace.PRECO_PADRAO);
    }

    @Dado("que {string} já gerou o pedido da proposta")
    public void jaGerouOPedido(String comprador) {
        assertNotNull(pedidos.daProposta(comprador, OpcaoFrete.ENVIO_SEGURO, FormaPagamento.PIX,
                PedidosDoCenario.endereco("50000-000"), null), this::erroAnterior);
    }

    @Quando("{string} gera o pedido da proposta com frete {string} e pagamento {string}")
    public void geraPedidoDaProposta(String comprador, String frete, String forma) {
        FormaPagamento formaPagamento = FormaPagamento.valueOf(forma);
        CartaoSimulado cartao = formaPagamento == FormaPagamento.CARTAO
                ? PedidosDoCenario.cartao(PedidosDoCenario.CARTAO_VALIDO)
                : null;
        pedidos.daProposta(comprador, OpcaoFrete.valueOf(frete), formaPagamento, PedidosDoCenario.endereco("50000-000"),
                cartao);
    }

    @Quando("{string} gera o pedido da proposta com o CEP {string}")
    public void geraPedidoComCep(String comprador, String cep) {
        contexto.executar(() -> pedidos.daProposta(comprador, OpcaoFrete.ENVIO_SEGURO, FormaPagamento.PIX,
                PedidosDoCenario.endereco(cep), null));
    }

    @Quando("{string} paga o pedido da proposta com o cartão {string}")
    public void pagaComCartao(String comprador, String numero) {
        pedidos.daProposta(comprador, OpcaoFrete.ENVIO_SEGURO, FormaPagamento.CARTAO,
                PedidosDoCenario.endereco("50000-000"), PedidosDoCenario.cartao(numero));
    }

    @Quando("{string} compra o anúncio direto com frete {string} e pagamento {string}")
    public void compraDireta(String comprador, String frete, String forma) {
        pedidos.compraDireta(comprador, OpcaoFrete.valueOf(frete), FormaPagamento.valueOf(forma));
    }

    // ---- Carteira (RN 5 e RN 6) ----

    @Dado("que {string} tem R$ {reais} na carteira")
    public void temNaCarteira(String nome, BigDecimal saldo) {
        Instant agora = Instant.now();
        carteiras.salvar(Carteira.reconstituir(dados.jogador(nome).id(), saldo, agora, agora));
    }

    @Entao("o saldo da carteira de {string} deve ser R$ {reais}")
    public void saldoDaCarteira(String nome, BigDecimal saldo) {
        dados.autenticar(nome);
        assertEquals(saldo, consultarPedidos.saldoDaCarteira());
    }

    // ---- Pedido em andamento (RN 7 a 10) ----

    @Dado("um pedido {string} de R$ {reais} entre o comprador {string} e o vendedor {string}")
    public void pedidoEm(String status, BigDecimal valor, String comprador, String vendedor) {
        pedidoEm(status, valor, comprador, vendedor, "PIX");
    }

    @Dado("um pedido {string} de R$ {reais} entre o comprador {string} e o vendedor {string} pago com {string}")
    public void pedidoEm(String status, BigDecimal valor, String comprador, String vendedor, String forma) {
        pedidos.criar(comprador, vendedor, valor, StatusPedido.valueOf(status), FormaPagamento.valueOf(forma));
    }

    @Dado("que o pedido foi enviado há {int} dias")
    @Dado("que o pedido foi pago há {int} dias")
    public void pedidoHaDias(int dias) {
        pedidos.recuar(Duration.ofDays(dias));
    }

    @Quando("{string} registra o código de rastreio {string}")
    public void registraRastreio(String usuario, String codigo) {
        dados.autenticar(usuario);
        contexto.obter(() -> acompanharPedido.registrarEnvio(pedidos.pedidoId(), codigo));
    }

    @Quando("{string} confirma o recebimento")
    public void confirmaRecebimento(String usuario) {
        dados.autenticar(usuario);
        contexto.obter(() -> acompanharPedido.confirmarRecebimento(pedidos.pedidoId()));
    }

    @Quando("a rotina de liberação de custódia é executada")
    public void rotinaDeCustodia() {
        prazos.liberarCustodiaPorTolerancia(Instant.now());
    }

    @Quando("a rotina de prazos de envio é executada")
    public void rotinaDePrazosDeEnvio() {
        prazos.cancelarPedidosSemEnvio(Instant.now());
    }

    @Entao("o pedido deve ter status {string}")
    public void pedidoDeveTerStatus(String status) {
        assertNotNull(pedidos.pedidoId(), this::erroAnterior);
        assertEquals(StatusPedido.valueOf(status), pedidos.pedido().status());
    }

    @Entao("o pagamento de R$ {reais} deve ficar {string}")
    public void pagamentoDeveFicar(BigDecimal valor, String status) {
        assertNotNull(pedidos.pedidoId(), this::erroAnterior);
        assertEquals(valor, pedidos.pedido().total());
        assertEquals(StatusPagamento.valueOf(status), pedidos.pedido().pagamento().status());
    }

    @Entao("o pagamento deve ficar {string}")
    public void pagamentoDeveFicar(String status) {
        assertEquals(StatusPagamento.valueOf(status), pedidos.pedido().pagamento().status());
    }

    @Entao("o total do pedido deve ser R$ {reais}")
    public void totalDoPedido(BigDecimal total) {
        assertNotNull(pedidos.pedidoId(), this::erroAnterior);
        assertEquals(total, pedidos.pedido().total());
    }

    // ---- RN 11 ----

    @Entao("o journal de {string} deve ter {int} entrada automática do tipo {string}")
    public void journalDeveTerEntrada(String nome, int quantidade, String tipo) {
        var entrada = entradas.buscarPorOrigem(TipoEventoPlataforma.valueOf(tipo), pedidos.pedidoId().toString());
        assertEquals(quantidade, entrada.isPresent() ? 1 : 0);
        assertEquals(dados.jogador(nome).id(), entrada.orElseThrow().donoId());
    }

    @Entao("as estatísticas do jogo devem contar {int} negociação")
    public void estatisticasDevemContar(int quantidade) {
        assertEquals(quantidade, estatisticas.buscarPorJogo(pedidos.pedido().jogoId()).orElseThrow().totalNegociacoes());
    }

    // ---- Consulta ----

    @Dado("que {string} possui {int} vendas e {int} compra")
    public void possuiVendasECompra(String nome, int vendas, int compras) {
        for (int i = 0; i < vendas; i++) {
            pedidoEm("AGUARDANDO_ENVIO", new BigDecimal("100.00"), "comprador " + (i + 1), nome);
        }
        for (int i = 0; i < compras; i++) {
            pedidoEm("ENVIADO", new BigDecimal("80.00"), nome, "vendedor " + (i + 1));
        }
    }

    @Quando("{string} consulta os seus pedidos")
    public void consultaPedidos(String nome) {
        dados.autenticar(nome);
        pedidosConsultados = consultarPedidos.listar();
    }

    @Quando("{string} consulta o pedido")
    public void consultaOPedido(String nome) {
        dados.autenticar(nome);
        contexto.obter(() -> consultarPedidos.detalhar(pedidos.pedidoId()));
    }

    @Entao("a consulta deve retornar {int} vendas e {int} compra com status de entrega e pagamento")
    public void consultaDeveRetornar(int vendas, int compras) {
        assertEquals(vendas, pedidosConsultados.stream().filter(p -> "VENDA".equals(p.papel())).count());
        assertEquals(compras, pedidosConsultados.stream().filter(p -> "COMPRA".equals(p.papel())).count());
        assertTrue(pedidosConsultados.stream()
                .allMatch(p -> p.pedido().status() != null && p.pedido().pagamento().status() != null));
    }

    // ---- Apoio ----

    private String erroAnterior() {
        return contexto.erro() == null ? null : "A ação foi recusada: " + contexto.erro().getMessage();
    }
}
