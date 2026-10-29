package com.g4.game_tracker_backend.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioStatusAlterado;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.CicloDeVidaAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;

/** T07: garante o contrato de status do anúncio usado por F-18, F-19 e F-20. */
class ContratoAnuncioTest {

    private static final UUID ANUNCIO = UUID.randomUUID();
    private static final UUID JOGO = UUID.randomUUID();
    private static final UUID VENDEDOR = UUID.randomUUID();

    @Test
    void fluxoCompletoDeVendaGeraEventosDeMudanca() {
        AnuncioStatusAlterado reserva = transicionar(StatusAnuncio.ATIVO, StatusAnuncio.EM_NEGOCIACAO);
        AnuncioStatusAlterado venda = transicionar(reserva.novoStatus(), StatusAnuncio.VENDIDO);

        assertEquals(StatusAnuncio.ATIVO, reserva.statusAnterior());
        assertFalse(reserva.vendido());
        assertTrue(venda.vendido());
        assertTrue(StatusAnuncio.VENDIDO.finalizado());
    }

    @Test
    void pausadoPodeSerReativado() {
        assertEquals(StatusAnuncio.ATIVO, transicionar(StatusAnuncio.PAUSADO, StatusAnuncio.ATIVO).novoStatus());
    }

    @Test
    void anuncioVendidoNaoMudaMaisDeStatus() {
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> transicionar(StatusAnuncio.VENDIDO, StatusAnuncio.ATIVO));
        assertEquals("O anúncio não pode passar de VENDIDO para ATIVO", erro.getMessage());
    }

    @Test
    void anuncioPausadoNaoEntraEmNegociacao() {
        assertThrows(RegraDeNegocioException.class,
                () -> transicionar(StatusAnuncio.PAUSADO, StatusAnuncio.EM_NEGOCIACAO));
        assertFalse(StatusAnuncio.PAUSADO.aceitaNegociacao());
    }

    @Test
    void vendidoECanceladoSaemDaBuscaPublica() {
        assertTrue(StatusAnuncio.ATIVO.visivelNaBuscaPublica());
        assertFalse(StatusAnuncio.VENDIDO.visivelNaBuscaPublica());
        assertFalse(StatusAnuncio.CANCELADO.visivelNaBuscaPublica());
    }

    private AnuncioStatusAlterado transicionar(StatusAnuncio de, StatusAnuncio para) {
        return CicloDeVidaAnuncio.transicionar(ANUNCIO, JOGO, VENDEDOR, de, para, Instant.now());
    }
}
