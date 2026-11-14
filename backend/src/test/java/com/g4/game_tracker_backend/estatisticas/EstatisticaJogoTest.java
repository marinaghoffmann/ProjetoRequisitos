package com.g4.game_tracker_backend.estatisticas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.NotasAgregadas;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.NotasDoJogo;

/** F-17: cálculo das médias e elegibilidade da ficha. */
class EstatisticaJogoTest {

    private static final UUID JOGO = UUID.randomUUID();
    private static final Instant AGORA = Instant.parse("2026-11-06T12:00:00Z");

    @Test
    void mediaTemDuasCasasEArredondaParaCima() {
        assertEquals(new BigDecimal("4.33"), NotasAgregadas.de(5, 4, 4).media());
        assertEquals(new BigDecimal("4.67"), NotasAgregadas.de(5, 5, 4).media());
        assertEquals(new BigDecimal("4.00"), NotasAgregadas.de(4).media());
    }

    @Test
    void semNotasNaoHaMedia() {
        assertNull(NotasAgregadas.vazias().media());
    }

    @Test
    void cadaCategoriaFicaComASuaMedia() {
        NotasDoJogo notas = new NotasDoJogo(NotasAgregadas.de(5, 4, 3), NotasAgregadas.de(1, 2), NotasAgregadas.de(2, 2),
                NotasAgregadas.de(3, 4), NotasAgregadas.de(4, 4), NotasAgregadas.vazias());

        EstatisticaJogo ficha = EstatisticaJogo.consolidar(JOGO, notas, 7, 8, 9, AGORA);

        assertEquals(new BigDecimal("4.00"), ficha.mediaGeral());
        assertEquals(new BigDecimal("1.50"), ficha.mediasPorCategoria().graficos());
        assertEquals(new BigDecimal("2.00"), ficha.mediasPorCategoria().som());
        assertEquals(new BigDecimal("3.50"), ficha.mediasPorCategoria().jogabilidade());
        assertEquals(new BigDecimal("4.00"), ficha.mediasPorCategoria().diversao());
        assertNull(ficha.mediasPorCategoria().inovacao());
        assertEquals(3, ficha.totalReviewsAtivas());
        assertEquals(7, ficha.totalConclusoes());
        assertEquals(8, ficha.totalEmWishlists());
        assertEquals(9, ficha.totalNegociacoes());
    }

    @Test
    void soEElegivelComTresReviewsAtivas() {
        assertFalse(fichaCom(NotasAgregadas.de(5, 5)).elegivelParaExibicao());
        assertTrue(fichaCom(NotasAgregadas.de(5, 5, 5)).elegivelParaExibicao());
    }

    @Test
    void jogoSemEventosTemFichaZerada() {
        EstatisticaJogo ficha = EstatisticaJogo.semDados(JOGO);

        assertNull(ficha.mediaGeral());
        assertEquals(0, ficha.totalReviewsAtivas());
        assertFalse(ficha.elegivelParaExibicao());
    }

    private static EstatisticaJogo fichaCom(NotasAgregadas geral) {
        NotasAgregadas vazias = NotasAgregadas.vazias();
        return EstatisticaJogo.consolidar(JOGO, new NotasDoJogo(geral, vazias, vazias, vazias, vazias, vazias), 0, 0,
                0, AGORA);
    }
}
