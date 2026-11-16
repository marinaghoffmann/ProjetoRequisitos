package com.g4.game_tracker_backend.similaridade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.CalculoSimilaridade;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.FeedbackDeSugestao;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.OrigemSimilaridade;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.PerfilJogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeJogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadePorEstudioStrategy;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadePorGeneroStrategy;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadePorTagStrategy;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.StatusSimilaridade;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.TipoFeedback;

class SimilaridadeTest {

    private static final Instant AGORA = Instant.parse("2026-11-16T12:00:00Z");
    private final UUID estudio = UUID.randomUUID();
    private final UUID jogoUm = UUID.randomUUID();
    private final UUID jogoDois = UUID.randomUUID();

    private PerfilJogo perfil(UUID jogo, Set<String> generos, Set<String> tags, UUID desenvolvedora) {
        return new PerfilJogo(jogo, generos, tags, desenvolvedora);
    }

    // ---- Strategy ----

    @Test
    void estrategiasCalculamJaccardEEstudio() {
        PerfilJogo a = perfil(jogoUm, Set.of("roguelike", "acao"), Set.of("dificil", "pixel"), estudio);
        PerfilJogo b = perfil(jogoDois, Set.of("roguelike"), Set.of("dificil", "metroidvania"), UUID.randomUUID());
        assertEquals(0, new BigDecimal("0.5").compareTo(new SimilaridadePorGeneroStrategy().calcular(a, b)));
        assertEquals(0, new BigDecimal("0.3333").compareTo(new SimilaridadePorTagStrategy().calcular(a, b)));
        assertEquals(BigDecimal.ZERO, new SimilaridadePorEstudioStrategy().calcular(a, b));
    }

    @Test
    void semDescritoresNaoHaSemelhanca() {
        PerfilJogo a = perfil(jogoUm, Set.of(), Set.of(), null);
        PerfilJogo b = perfil(jogoDois, Set.of(), Set.of(), null);
        assertEquals(new BigDecimal("0.00"), CalculoSimilaridade.padrao().calcular(a, b));
    }

    @Test
    void calculoCombinaPesosESimetrico() {
        PerfilJogo a = perfil(jogoUm, Set.of("roguelike"), Set.of("dificil"), estudio);
        PerfilJogo b = perfil(jogoDois, Set.of("roguelike"), Set.of("dificil", "pixel"), estudio);
        CalculoSimilaridade calculo = CalculoSimilaridade.padrao();
        // 0,5 x 1 + 0,3 x 0,5 + 0,2 x 1 = 0,85
        assertEquals(new BigDecimal("0.85"), calculo.calcular(a, b));
        assertEquals(calculo.calcular(a, b), calculo.calcular(b, a));
    }

    @Test
    void pesosPrecisamSomarUm() {
        assertThrows(RegraDeNegocioException.class, () -> new CalculoSimilaridade(java.util.List.of(
                new CalculoSimilaridade.Criterio(new SimilaridadePorGeneroStrategy(), new BigDecimal("0.4")))));
    }

    // ---- RN 1 ----

    @Test
    void parENormalizadoEUmJogoNaoESimilarAEleMesmo() {
        SimilaridadeJogo ab = SimilaridadeJogo.automatica(jogoUm, jogoDois, new BigDecimal("0.7"), AGORA);
        SimilaridadeJogo ba = SimilaridadeJogo.automatica(jogoDois, jogoUm, new BigDecimal("0.7"), AGORA);
        assertEquals(ab.jogoAId(), ba.jogoAId());
        assertEquals(jogoDois, ab.outroJogo(jogoUm));
        assertEquals(jogoUm, ab.outroJogo(jogoDois));
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> SimilaridadeJogo.automatica(jogoUm, jogoUm, BigDecimal.ONE, AGORA));
        assertEquals("Um jogo não pode ser similar a ele mesmo", erro.getMessage());
    }

    @Test
    void grauFicaEntreZeroEUm() {
        assertThrows(RegraDeNegocioException.class,
                () -> SimilaridadeJogo.manual(jogoUm, jogoDois, new BigDecimal("1.2"), AGORA));
    }

    // ---- RN 2 ----

    @Test
    void maisDeQuinzeNegativosRebaixamOGrau() {
        SimilaridadeJogo sugestao = SimilaridadeJogo.automatica(jogoUm, jogoDois, new BigDecimal("0.80"), AGORA);
        SimilaridadeJogo com15 = sugestao.comFeedbacks(0, 15, AGORA);
        SimilaridadeJogo com16 = sugestao.comFeedbacks(0, 16, AGORA);
        assertFalse(com15.rebaixada());
        assertEquals(new BigDecimal("0.80"), com15.grauRelevancia());
        assertTrue(com16.rebaixada());
        assertEquals(new BigDecimal("0.40"), com16.grauRelevancia());
        assertEquals(new BigDecimal("0.80"), com16.comFeedbacks(0, 15, AGORA).grauRelevancia());
    }

    @Test
    void feedbackSoPeloAutor() {
        FeedbackDeSugestao feedback = FeedbackDeSugestao.registrar(jogoUm, UUID.randomUUID(), TipoFeedback.UTIL,
                AGORA);
        assertEquals(TipoFeedback.NAO_RELEVANTE,
                feedback.editar(jogoUm, TipoFeedback.NAO_RELEVANTE, AGORA).tipo());
        assertThrows(AcessoNegadoException.class,
                () -> feedback.editar(UUID.randomUUID(), TipoFeedback.NAO_RELEVANTE, AGORA));
    }

    // ---- RN 3 ----

    @Test
    void recalculoNaoMexeEmConexaoManualNemRemovida() {
        SimilaridadeJogo manual = SimilaridadeJogo.manual(jogoUm, jogoDois, new BigDecimal("0.95"), AGORA);
        assertSame(manual, manual.recalcular(new BigDecimal("0.30"), AGORA));

        SimilaridadeJogo removida = SimilaridadeJogo.automatica(jogoUm, jogoDois, new BigDecimal("0.60"), AGORA)
                .remover(AGORA);
        assertEquals(StatusSimilaridade.REMOVIDA, removida.recalcular(new BigDecimal("0.90"), AGORA).status());
        assertFalse(removida.visivel());

        SimilaridadeJogo reativada = removida.definirManual(new BigDecimal("0.50"), AGORA);
        assertEquals(OrigemSimilaridade.MANUAL, reativada.origem());
        assertTrue(reativada.visivel());
    }
}
