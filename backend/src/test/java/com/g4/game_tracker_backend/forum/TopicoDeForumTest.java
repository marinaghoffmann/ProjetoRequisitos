package com.g4.game_tracker_backend.forum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.forum.StatusTopico;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;

class TopicoDeForumTest {

    private static final Instant AGORA = Instant.parse("2026-10-30T12:00:00Z");
    private final UUID autor = UUID.randomUUID();
    private final UUID outro = UUID.randomUUID();

    private TopicoDeForum topico() {
        return TopicoDeForum.criar(autor, null, " ", "Dúvida", "Como começo?", AGORA);
    }

    @Test
    void topicoSemCategoriaViraGeral() {
        TopicoDeForum topico = topico();
        assertEquals(TopicoDeForum.CATEGORIA_GERAL, topico.categoria());
        assertEquals(StatusTopico.ABERTO, topico.status());
        assertEquals(0, topico.totalRespostas());
    }

    @Test
    void responderAtualizaContadorEUltimaAtividade() {
        Instant depois = AGORA.plusSeconds(60);
        TopicoDeForum topico = topico().responder(outro, null, "Comece pelo tutorial", depois);
        assertEquals(1, topico.totalRespostas());
        assertEquals(depois, topico.dataUltimaAtividade());
    }

    @Test
    void respostaPaiPrecisaSerDoMesmoTopico() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> topico().responder(outro, UUID.randomUUID(), "Oi", AGORA));
    }

    @Test
    void exclusaoDeRespostaELogicaENaoConta() {
        TopicoDeForum topico = topico().responder(outro, null, "Oi", AGORA);
        UUID respostaId = topico.ultimaResposta().id();
        TopicoDeForum depois = topico.excluirResposta(respostaId, outro, AGORA.plusSeconds(5));
        assertEquals(0, depois.totalRespostas());
        assertTrue(depois.resposta(respostaId).removida());
        assertThrows(RegraDeNegocioException.class,
                () -> depois.responder(autor, respostaId, "Respondendo à removida", AGORA));
    }

    @Test
    void prazoDeEdicaoValeAteExatamente48Horas() {
        TopicoDeForum topico = topico().responder(outro, null, "Oi", AGORA);
        UUID respostaId = topico.ultimaResposta().id();
        Instant limite = AGORA.plus(Duration.ofHours(48));
        assertEquals("Editado", topico.editarResposta(respostaId, outro, "Editado", limite)
                .resposta(respostaId).conteudo());
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> topico.editarResposta(respostaId, outro, "Tarde", limite.plusSeconds(1)));
        assertEquals("O prazo de 48 horas para edição terminou", erro.getMessage());
    }

    @Test
    void apenasOAutorAlteraOTopico() {
        AcessoNegadoException erro = assertThrows(AcessoNegadoException.class,
                () -> topico().editar(outro, "Outro título", "Outro conteúdo", null, AGORA));
        assertEquals("Apenas o autor pode alterar esta postagem", erro.getMessage());
    }

    @Test
    void topicoEncerradoBloqueiaTudo() {
        TopicoDeForum topico = topico().responder(outro, null, "Oi", AGORA);
        UUID respostaId = topico.ultimaResposta().id();
        TopicoDeForum encerrado = topico.alterarStatus(autor, StatusTopico.ENCERRADO, AGORA);
        assertEquals("Este tópico está encerrado", assertThrows(RegraDeNegocioException.class,
                () -> encerrado.excluirResposta(respostaId, outro, AGORA)).getMessage());
        assertThrows(RegraDeNegocioException.class,
                () -> encerrado.alterarStatus(autor, StatusTopico.ABERTO, AGORA));
        assertThrows(RegraDeNegocioException.class, () -> encerrado.remover(autor));
    }
}
