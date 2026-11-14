package com.g4.game_tracker_backend.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.PapelNaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.PedidoAvaliavel;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.ReputacaoUsuario;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.TagComportamento;

class ReputacaoTest {

    private static final Instant AGORA = Instant.parse("2026-11-09T12:00:00Z");
    private final UUID comprador = UUID.randomUUID();
    private final UUID vendedor = UUID.randomUUID();

    private PedidoAvaliavel concluidoHa(Duration tempo) {
        return new PedidoAvaliavel(UUID.randomUUID(), comprador, vendedor, true, AGORA.minus(tempo));
    }

    // ---- Avaliação ----

    @Test
    void compradorAvaliaOVendedorEVendedorAvaliaOComprador() {
        PedidoAvaliavel pedido = concluidoHa(Duration.ofDays(1));

        AvaliacaoMarketplace doComprador = AvaliacaoMarketplace.criar(pedido, comprador, 5,
                Set.of(TagComportamento.ENVIO_RAPIDO), "  Chegou rápido  ", AGORA);
        AvaliacaoMarketplace doVendedor = AvaliacaoMarketplace.criar(pedido, vendedor, 4, null, null, AGORA);

        assertEquals(vendedor, doComprador.avaliadoId());
        assertEquals(PapelNaNegociacao.VENDEDOR, doComprador.papelAvaliado());
        assertEquals("Chegou rápido", doComprador.comentario());
        assertEquals(comprador, doVendedor.avaliadoId());
        assertEquals(PapelNaNegociacao.COMPRADOR, doVendedor.papelAvaliado());
    }

    @Test
    void quemNaoParticipouDoPedidoNaoAvalia() {
        AcessoNegadoException erro = assertThrows(AcessoNegadoException.class, () -> AvaliacaoMarketplace.criar(
                concluidoHa(Duration.ofDays(1)), UUID.randomUUID(), 5, null, null, AGORA));
        assertEquals("Apenas o comprador e o vendedor do pedido podem avaliar", erro.getMessage());
    }

    @Test
    void pedidoNaoConcluidoNaoPodeSerAvaliado() {
        PedidoAvaliavel enviado = new PedidoAvaliavel(UUID.randomUUID(), comprador, vendedor, false, null);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> AvaliacaoMarketplace.criar(enviado, comprador, 5, null, null, AGORA));
        assertEquals("Só é possível avaliar pedidos concluídos", erro.getMessage());
    }

    @Test
    void prazoDeQuinzeDiasValeAteOUltimoInstante() {
        AvaliacaoMarketplace.criar(concluidoHa(Duration.ofDays(15)), comprador, 3, null, null, AGORA);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class, () -> AvaliacaoMarketplace.criar(
                concluidoHa(Duration.ofDays(15).plusSeconds(1)), comprador, 3, null, null, AGORA));
        assertEquals("O prazo de 15 dias para avaliar terminou", erro.getMessage());
    }

    @Test
    void notaForaDeUmACincoERecusada() {
        PedidoAvaliavel pedido = concluidoHa(Duration.ofDays(1));
        assertThrows(RegraDeNegocioException.class,
                () -> AvaliacaoMarketplace.criar(pedido, comprador, 0, null, null, AGORA));
        assertThrows(RegraDeNegocioException.class,
                () -> AvaliacaoMarketplace.criar(pedido, comprador, 6, null, null, AGORA));
    }

    // ---- Reputação ----

    @Test
    void reputacaoSemAvaliacoesNaoTemMedia() {
        assertNull(ReputacaoUsuario.semAvaliacoes(vendedor).notaMedia());
    }

    @Test
    void novaAvaliacaoRecalculaAMediaESomaUmaNegociacao() {
        ReputacaoUsuario reputacao = ReputacaoUsuario.reconstituir(vendedor, 8, 2, 2, AGORA)
                .registrarAvaliacao(5, AGORA);

        assertEquals(new BigDecimal("4.33"), reputacao.notaMedia());
        assertEquals(3, reputacao.totalAvaliacoes());
        assertEquals(3, reputacao.negociacoesBemSucedidas());
    }

    @Test
    void mediaNaoAcumulaErroDeArredondamento() {
        ReputacaoUsuario reputacao = ReputacaoUsuario.semAvaliacoes(vendedor);
        for (int i = 0; i < 3; i++) {
            reputacao = reputacao.registrarAvaliacao(5, AGORA).registrarAvaliacao(4, AGORA)
                    .registrarAvaliacao(4, AGORA);
        }
        // 39 estrelas em 9 avaliações = 4.333..., e não a soma de médias arredondadas.
        assertEquals(new BigDecimal("4.33"), reputacao.notaMedia());
        assertEquals(39, reputacao.somaEstrelas());
    }
}
