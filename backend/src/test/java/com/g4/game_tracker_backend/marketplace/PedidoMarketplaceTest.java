package com.g4.game_tracker_backend.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CalendarioDiasUteis;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CartaoSimulado;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.Carteira;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.EnderecoEntrega;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.FormaPagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.OpcaoFrete;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.OrigemPedido;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.StatusPagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.StatusPedido;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.TransacaoPagamento;

class PedidoMarketplaceTest {

    /** Sexta-feira, 13/11/2026, 10h em Brasília. */
    private static final Instant SEXTA = LocalDateTime.of(2026, 11, 13, 10, 0)
            .atZone(CalendarioDiasUteis.FUSO).toInstant();

    private final UUID comprador = UUID.randomUUID();
    private final UUID vendedor = UUID.randomUUID();

    @Test
    void totalSomaItemEFrete() {
        assertEquals(new BigDecimal("138.90"), PedidoMarketplace.calcularTotal(new BigDecimal("120.00"),
                OpcaoFrete.ENVIO_SEGURO));
        assertEquals(new BigDecimal("32.90"), PedidoMarketplace.calcularTotal(BigDecimal.ZERO, OpcaoFrete.EXPRESSO));
    }

    @Test
    void prazoDeEnvioPulaOFimDeSemana() {
        Instant quarta = LocalDateTime.of(2026, 11, 18, 10, 0).atZone(CalendarioDiasUteis.FUSO).toInstant();
        assertEquals(quarta, CalendarioDiasUteis.somarDiasUteis(SEXTA, 3));
        assertEquals(quarta, pedido().prazoEnvio());
    }

    @Test
    void pedidoNascePagoEmCustodiaAguardandoEnvio() {
        PedidoMarketplace pedido = pedido();
        assertEquals(StatusPedido.AGUARDANDO_ENVIO, pedido.status());
        assertEquals(StatusPagamento.EM_CUSTODIA, pedido.pagamento().status());
        assertEquals(new BigDecimal("138.90"), pedido.total());
    }

    @Test
    void naoCompraOProprioAnuncio() {
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> PedidoMarketplace.gerar(OrigemPedido.COMPRA_DIRETA, UUID.randomUUID(), UUID.randomUUID(), "Hades",
                        null, null, vendedor, vendedor, new BigDecimal("120.00"), OpcaoFrete.ENVIO_SEGURO, endereco(),
                        pagamento("138.90"), SEXTA));
        assertEquals("Você não pode comprar o próprio anúncio", erro.getMessage());
    }

    @Test
    void soOVendedorRegistraOEnvioDentroDoPrazo() {
        PedidoMarketplace pedido = pedido();
        assertEquals("Apenas o vendedor registra o envio", assertThrows(RegraDeNegocioException.class,
                () -> pedido.registrarEnvio(comprador, "BR123456789", SEXTA)).getMessage());
        assertThrows(AcessoNegadoException.class, () -> pedido.registrarEnvio(UUID.randomUUID(), "BR1", SEXTA));
        assertEquals("O código de rastreio deve ter de 3 a 60 caracteres", assertThrows(RegraDeNegocioException.class,
                () -> pedido.registrarEnvio(vendedor, "BR", SEXTA)).getMessage());
        assertEquals("O prazo de 3 dias úteis para o envio terminou", assertThrows(RegraDeNegocioException.class,
                () -> pedido.registrarEnvio(vendedor, "BR123456789", SEXTA.plus(Duration.ofDays(6)))).getMessage());

        PedidoMarketplace enviado = pedido.registrarEnvio(vendedor, "BR123456789", SEXTA.plus(Duration.ofDays(1)));
        assertEquals(StatusPedido.ENVIADO, enviado.status());
        assertEquals("BR123456789", enviado.codigoRastreio());
    }

    @Test
    void recebimentoSoPeloCompradorDepoisDoEnvio() {
        PedidoMarketplace pedido = pedido();
        assertEquals("Só é possível confirmar o recebimento de um pedido enviado",
                assertThrows(RegraDeNegocioException.class, () -> pedido.confirmarRecebimento(comprador, SEXTA))
                        .getMessage());

        PedidoMarketplace enviado = pedido.registrarEnvio(vendedor, "BR123456789", SEXTA);
        assertEquals("Apenas o comprador confirma o recebimento", assertThrows(RegraDeNegocioException.class,
                () -> enviado.confirmarRecebimento(vendedor, SEXTA)).getMessage());

        PedidoMarketplace concluido = enviado.confirmarRecebimento(comprador, SEXTA.plus(Duration.ofDays(2)));
        assertEquals(StatusPedido.CONCLUIDO, concluido.status());
        assertEquals(StatusPagamento.LIBERADO, concluido.pagamento().status());
        assertEquals(SEXTA.plus(Duration.ofDays(2)), concluido.concluidoEm());
    }

    @Test
    void custodiaLiberadaSeteDiasAposOEnvio() {
        PedidoMarketplace enviado = pedido().registrarEnvio(vendedor, "BR123456789", SEXTA);
        assertFalse(enviado.toleranciaVencida(SEXTA.plus(Duration.ofDays(6))));
        assertTrue(enviado.toleranciaVencida(SEXTA.plus(Duration.ofDays(7))));
        assertEquals(StatusPedido.CONCLUIDO, enviado.concluirPorTolerancia(SEXTA.plus(Duration.ofDays(7))).status());
    }

    @Test
    void semRastreioNoPrazoOPedidoECanceladoEReembolsado() {
        PedidoMarketplace pedido = pedido();
        assertFalse(pedido.prazoDeEnvioVencido(SEXTA.plus(Duration.ofDays(4))));
        assertTrue(pedido.prazoDeEnvioVencido(SEXTA.plus(Duration.ofDays(6))));

        PedidoMarketplace cancelado = pedido.cancelarPorFaltaDeEnvio(SEXTA.plus(Duration.ofDays(6)));
        assertEquals(StatusPedido.CANCELADO, cancelado.status());
        assertEquals(StatusPagamento.REEMBOLSADO, cancelado.pagamento().status());
    }

    @Test
    void enderecoExigeCamposObrigatoriosECepFormatado() {
        assertEquals("O destinatário deve ter de 3 a 80 caracteres", assertThrows(RegraDeNegocioException.class,
                () -> new EnderecoEntrega("Jo", "50000-000", "Rua A", "10", null, "Recife", "PE")).getMessage());
        assertEquals("O CEP deve estar no formato 00000-000", assertThrows(RegraDeNegocioException.class,
                () -> new EnderecoEntrega("João", "50000000", "Rua A", "10", null, "Recife", "PE")).getMessage());
        assertEquals("A UF deve ter 2 letras", assertThrows(RegraDeNegocioException.class,
                () -> new EnderecoEntrega("João", "50000-000", "Rua A", "10", null, "Recife", "Pernambuco"))
                .getMessage());
        assertEquals("PE", new EnderecoEntrega("João", "50000-000", "Rua A", "10", " ", "Recife", "pe").uf());
    }

    @Test
    void cartaoValidaLuhnValidadeECvv() {
        YearMonth novembro = YearMonth.of(2026, 11);
        assertEquals("1111", new CartaoSimulado("4111 1111 1111 1111", "JOAO S", "12/28", "123").validar(novembro));
        assertEquals("2222", new CartaoSimulado("4222222222222", "JOAO S", "11/26", "1234").validar(novembro));

        assertEquals("Número do cartão inválido", assertThrows(RegraDeNegocioException.class,
                () -> new CartaoSimulado("4111111111111112", "JOAO S", "12/28", "123").validar(novembro)).getMessage());
        assertEquals("Cartão vencido", assertThrows(RegraDeNegocioException.class,
                () -> new CartaoSimulado("4111111111111111", "JOAO S", "10/26", "123").validar(novembro)).getMessage());
        assertEquals("O CVV deve ter 3 ou 4 dígitos", assertThrows(RegraDeNegocioException.class,
                () -> new CartaoSimulado("4111111111111111", "JOAO S", "12/28", "12").validar(novembro)).getMessage());
        assertFalse(new CartaoSimulado("4111111111111111", "JOAO S", "12/28", "123").toString().contains("41111"));
    }

    @Test
    void carteiraComecaCom500EDebitaSoComSaldo() {
        Carteira carteira = Carteira.abrir(comprador, SEXTA);
        assertEquals(new BigDecimal("500.00"), carteira.saldo());
        assertEquals(new BigDecimal("361.10"), carteira.debitar(new BigDecimal("138.90"), SEXTA).saldo());
        assertEquals("Saldo insuficiente na carteira", assertThrows(RegraDeNegocioException.class,
                () -> carteira.debitar(new BigDecimal("500.01"), SEXTA)).getMessage());
        assertEquals(new BigDecimal("638.90"), carteira.creditar(new BigDecimal("138.90"), SEXTA).saldo());
    }

    private PedidoMarketplace pedido() {
        return PedidoMarketplace.gerar(OrigemPedido.PROPOSTA, UUID.randomUUID(), UUID.randomUUID(), "Hades",
                UUID.randomUUID(), null, comprador, vendedor, new BigDecimal("120.00"), OpcaoFrete.ENVIO_SEGURO,
                endereco(), pagamento("138.90"), SEXTA);
    }

    private static TransacaoPagamento pagamento(String valor) {
        return TransacaoPagamento.emCustodia(FormaPagamento.PIX, new BigDecimal(valor), null, SEXTA);
    }

    private static EnderecoEntrega endereco() {
        return new EnderecoEntrega("João Silva", "50000-000", "Rua da Aurora", "100", null, "Recife", "PE");
    }
}
