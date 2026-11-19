package com.g4.game_tracker_backend.forum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.forum.IteradorDeRespostas.RespostaNaArvore;
import com.g4.game_tracker_backend.dominio.forum.RespostaForum;
import com.g4.game_tracker_backend.dominio.forum.StatusTopico;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;

/** Padrão Iterator da F-13: percurso em profundidade das respostas encadeadas. */
class IteradorDeRespostasTest {

    private static final Instant T0 = Instant.parse("2026-11-01T10:00:00Z");
    private final UUID autor = UUID.randomUUID();

    private TopicoDeForum topico() {
        return TopicoDeForum.criar(autor, null, "Geral", "Chefe final", "Como passo?", T0);
    }

    private static UUID idDa(TopicoDeForum topico, String texto) {
        return topico.respostas().stream().filter(r -> r.conteudo().equals(texto)).findFirst().orElseThrow().id();
    }

    private static List<String> percurso(TopicoDeForum topico) {
        List<String> visitadas = new ArrayList<>();
        for (RespostaNaArvore r : topico.arvoreDeRespostas()) {
            visitadas.add("  ".repeat(r.nivel()) + r.resposta().conteudo());
        }
        return visitadas;
    }

    @Test
    void visitaCadaRespostaAntesDasFilhasEAsIrmasPorPublicacao() {
        TopicoDeForum t = topico()
                .responder(autor, null, "A", T0.plusSeconds(1))
                .responder(autor, null, "B", T0.plusSeconds(2));
        t = t.responder(autor, idDa(t, "A"), "A.1", T0.plusSeconds(3));
        t = t.responder(autor, idDa(t, "B"), "B.1", T0.plusSeconds(4));
        t = t.responder(autor, idDa(t, "A"), "A.2", T0.plusSeconds(5));
        t = t.responder(autor, idDa(t, "A.1"), "A.1.a", T0.plusSeconds(6));

        assertEquals(List.of("A", "  A.1", "    A.1.a", "  A.2", "B", "  B.1"), percurso(t));
    }

    @Test
    void ordemNaoDependeDaOrdemDaListaInterna() {
        UUID a = UUID.randomUUID();
        RespostaForum filha = RespostaForum.reconstituir(UUID.randomUUID(), autor, a, "A.1", false,
                T0.plusSeconds(2), null);
        RespostaForum pai = RespostaForum.reconstituir(a, autor, null, "A", false, T0.plusSeconds(1), null);
        TopicoDeForum t = TopicoDeForum.reconstituir(UUID.randomUUID(), autor, null, "Geral", "Título", "Texto",
                StatusTopico.ABERTO, false, T0, null, T0, List.of(filha, pai));

        assertEquals(List.of("A", "  A.1"), percurso(t));
    }

    @Test
    void respostaComPaiForaDoTopicoViraRespostaDireta() {
        RespostaForum orfa = RespostaForum.reconstituir(UUID.randomUUID(), autor, UUID.randomUUID(), "Órfã",
                false, T0, null);
        TopicoDeForum t = TopicoDeForum.reconstituir(UUID.randomUUID(), autor, null, "Geral", "Título", "Texto",
                StatusTopico.ABERTO, false, T0, null, T0, List.of(orfa));

        assertEquals(List.of("Órfã"), percurso(t));
    }

    @Test
    void cadaPercursoComecaDoInicioEAcabaSemElementos() {
        TopicoDeForum t = topico().responder(autor, null, "A", T0.plusSeconds(1));
        assertEquals(percurso(t), percurso(t));

        Iterator<RespostaNaArvore> iterador = t.arvoreDeRespostas().iterator();
        iterador.next();
        assertFalse(iterador.hasNext());
        assertThrows(NoSuchElementException.class, iterador::next);
    }

    @Test
    void topicoSemRespostasNaoTemPercurso() {
        assertFalse(topico().arvoreDeRespostas().iterator().hasNext());
    }
}
