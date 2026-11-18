package com.g4.game_tracker_backend.reviews;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.avaliacao.reviews.CategoriaReview;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.NotaInformada;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ResumoDeNotas;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.Review;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.SomaDeNotas;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

class ReviewTest {

    private static final Instant AGORA = Instant.parse("2026-11-16T12:00:00Z");
    private final UUID autor = UUID.randomUUID();
    private final UUID jogo = UUID.randomUUID();

    private Review review(Integer notaGeral, List<NotaInformada> notas) {
        return Review.publicar(autor, jogo, notaGeral, notas, "  Muito bom  ", AGORA);
    }

    @Test
    void publicaComNotasPorCategoriaOrdenadas() {
        Review review = review(5, List.of(new NotaInformada(CategoriaReview.SOM, 4),
                new NotaInformada(CategoriaReview.GRAFICOS, 5)));
        assertEquals(5, review.notaGeral().valor());
        assertEquals("Muito bom", review.texto());
        assertEquals(List.of(CategoriaReview.GRAFICOS, CategoriaReview.SOM),
                review.notasPorCategoria().stream().map(nota -> nota.categoria()).toList());
    }

    @Test
    void notasPorCategoriaSaoOpcionais() {
        assertTrue(review(3, null).notasPorCategoria().isEmpty());
    }

    @Test
    void notaForaDoIntervaloERecusada() {
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class, () -> review(6, List.of()));
        assertEquals("A nota deve estar entre 1 e 5", erro.getMessage());
        assertThrows(RegraDeNegocioException.class, () -> review(null, List.of()));
        assertThrows(RegraDeNegocioException.class,
                () -> review(4, List.of(new NotaInformada(CategoriaReview.SOM, 0))));
    }

    @Test
    void categoriaRepetidaERecusada() {
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class, () -> review(4,
                List.of(new NotaInformada(CategoriaReview.SOM, 4), new NotaInformada(CategoriaReview.SOM, 5))));
        assertEquals("Cada categoria só pode receber uma nota", erro.getMessage());
    }

    @Test
    void textoTemLimite() {
        assertThrows(RegraDeNegocioException.class, () -> Review.publicar(autor, jogo, 4, List.of(),
                "a".repeat(Review.LIMITE_DO_TEXTO + 1), AGORA));
    }

    @Test
    void autorEditaMantendoOIdDaCategoriaQueContinua() {
        Review original = review(4, List.of(new NotaInformada(CategoriaReview.SOM, 4),
                new NotaInformada(CategoriaReview.INOVACAO, 2)));
        UUID idDoSom = original.notasPorCategoria().get(0).id();
        Instant depois = AGORA.plusSeconds(60);

        Review editada = original.editar(autor, 5, List.of(new NotaInformada(CategoriaReview.SOM, 5)), "Melhor",
                depois);

        assertEquals(5, editada.notaGeral().valor());
        assertEquals(1, editada.notasPorCategoria().size());
        assertEquals(idDoSom, editada.notasPorCategoria().get(0).id());
        assertEquals(5, editada.notaDe(CategoriaReview.SOM).orElseThrow().valor());
        assertEquals(AGORA, editada.criadaEm());
        assertEquals(depois, editada.atualizadaEm());
    }

    @Test
    void apenasOAutorAlteraAReview() {
        Review review = review(4, List.of());
        UUID outro = UUID.randomUUID();
        AcessoNegadoException erro = assertThrows(AcessoNegadoException.class,
                () -> review.editar(outro, 5, List.of(), "", AGORA));
        assertEquals("Apenas o autor pode alterar a review", erro.getMessage());
        assertThrows(AcessoNegadoException.class, () -> review.exigirAutor(outro));
    }

    @Test
    void mediasComDuasCasasEArredondamentoHalfUp() {
        ResumoDeNotas resumo = new ResumoDeNotas(new SomaDeNotas(13, 3),
                Map.of(CategoriaReview.GRAFICOS, new SomaDeNotas(9, 2)));
        assertEquals(new BigDecimal("4.33"), resumo.geral().media());
        assertEquals(new BigDecimal("4.50"), resumo.daCategoria(CategoriaReview.GRAFICOS).media());
        assertNull(resumo.daCategoria(CategoriaReview.SOM).media());
        assertEquals(5, resumo.mediasPorCategoria().size());
    }
}
