package com.g4.game_tracker_backend.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.EstadoConservacao;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.FiltroAnuncios;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.MidiaJogo;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.MudancaDeStatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;

/** F-18: regras do agregado AnuncioMarketplace. */
class AnuncioMarketplaceTest {

    private static final UUID VENDEDOR = UUID.randomUUID();
    private static final UUID JOGO = UUID.randomUUID();

    @Test
    void anuncioNasceAtivoComFotosOrdenadas() {
        AnuncioMarketplace anuncio = criar(new BigDecimal("59.9"), List.of("/a.jpg", "/b.jpg"));

        assertEquals(StatusAnuncio.ATIVO, anuncio.status());
        assertEquals(new BigDecimal("59.90"), anuncio.preco());
        assertEquals(1, anuncio.fotos().get(0).ordem());
        assertEquals("/b.jpg", anuncio.fotos().get(1).urlFoto());
    }

    @Test
    void precoPrecisaSerPositivo() {
        var erro = assertThrows(RegraDeNegocioException.class, () -> criar(BigDecimal.ZERO, List.of("/a.jpg")));
        assertEquals("O preço deve ser maior que zero", erro.getMessage());
    }

    @Test
    void anuncioSemFotoERecusado() {
        var erro = assertThrows(RegraDeNegocioException.class, () -> criar(BigDecimal.TEN, List.of()));
        assertEquals("O anúncio precisa de pelo menos 1 foto", erro.getMessage());
    }

    @Test
    void mudancaDeStatusGeraEvento() {
        MudancaDeStatusAnuncio mudanca = criar(BigDecimal.TEN, List.of("/a.jpg"))
                .mudarStatus(StatusAnuncio.PAUSADO, Instant.now());

        assertEquals(StatusAnuncio.PAUSADO, mudanca.anuncio().status());
        assertEquals(StatusAnuncio.ATIVO, mudanca.evento().statusAnterior());
        assertEquals(StatusAnuncio.PAUSADO, mudanca.evento().novoStatus());
    }

    @Test
    void anuncioCanceladoNaoPodeSerEditado() {
        AnuncioMarketplace cancelado = criar(BigDecimal.TEN, List.of("/a.jpg"))
                .mudarStatus(StatusAnuncio.CANCELADO, Instant.now()).anuncio();

        var erro = assertThrows(RegraDeNegocioException.class, () -> cancelado.editar("Novo título",
                EstadoConservacao.NOVO, BigDecimal.ONE, TipoNegociacao.VENDA, MidiaJogo.FISICO, "Recife",
                List.of("/a.jpg"), Instant.now()));
        assertEquals("Anúncios vendidos ou cancelados não podem ser alterados", erro.getMessage());
    }

    @Test
    void buscaPorVendaIncluiAnunciosQueAceitamAmbos() {
        FiltroAnuncios filtro = new FiltroAnuncios(null, null, null, null, null, TipoNegociacao.VENDA, null);

        assertEquals(Set.of(TipoNegociacao.VENDA, TipoNegociacao.AMBOS), filtro.tiposAceitos());
        assertTrue(!FiltroAnuncios.statusVisiveis().contains(StatusAnuncio.VENDIDO));
    }

    private static AnuncioMarketplace criar(BigDecimal preco, List<String> fotos) {
        return AnuncioMarketplace.criar(VENDEDOR, JOGO, "Hades", EstadoConservacao.USADO, preco,
                TipoNegociacao.VENDA, MidiaJogo.FISICO, "Recife - PE", fotos, Instant.now());
    }
}
