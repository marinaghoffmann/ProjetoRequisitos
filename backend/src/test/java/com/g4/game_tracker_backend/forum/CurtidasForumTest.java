package com.g4.game_tracker_backend.forum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.g4.game_tracker_backend.aplicacao.forum.CurtidasForum;
import com.g4.game_tracker_backend.aplicacao.forum.GerenciarRespostas;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.forum.FiltroTopicos;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForumRepositorio;

@SpringBootTest
@ActiveProfiles("test")
class CurtidasForumTest {
    @Autowired CurtidasForum curtidas;
    @Autowired GerenciarRespostas respostas;
    @Autowired TopicoDeForumRepositorio topicos;
    @MockitoBean VerificadorDeAcesso acesso;
    UUID usuario = UUID.randomUUID();

    @BeforeEach void autenticar() { when(acesso.usuarioAtualId()).thenReturn(usuario); }

    @Test void curtidaRepetidaEOrdenacaoEmAlta() {
        String categoria = "Curtidas " + UUID.randomUUID();
        UUID antigo = topicos.salvar(TopicoDeForum.criar(usuario, null, categoria,
                "Antigo", "Conteúdo", Instant.now().minusSeconds(3600))).id();
        UUID novo = topicos.salvar(TopicoDeForum.criar(usuario, null, categoria,
                "Novo", "Conteúdo", Instant.now())).id();
        curtidas.definir(antigo, null, true);
        curtidas.definir(antigo, null, true);
        assertEquals(1, curtidas.resumo(antigo, usuario).totalTopico());
        assertEquals(antigo, topicos.listar(new FiltroTopicos(null, categoria, true, null, true), 1, 5)
                .itens().getFirst().id());
        curtidas.definir(antigo, null, false);
        curtidas.definir(antigo, null, false);
        assertEquals(0, curtidas.resumo(antigo, usuario).total());
        assertEquals(novo, topicos.listar(new FiltroTopicos(null, categoria, true, null, true), 1, 5)
                .itens().getFirst().id());
    }

    @Test void removerRespostaDescartaCurtidasSemApagarArvore() {
        UUID topicoId = topicos.salvar(TopicoDeForum.criar(usuario, null, "Ajuda",
                "Resposta curtida", "Conteúdo", Instant.now())).id();
        UUID respostaId = respostas.responder(topicoId, null, "Primeira resposta").id();
        curtidas.definir(topicoId, respostaId, true);
        assertEquals(1, curtidas.resumo(topicoId, usuario).total());
        respostas.excluir(topicoId, respostaId);
        assertEquals(0, curtidas.resumo(topicoId, usuario).total());
        assertFalse(topicos.buscarPorId(topicoId).orElseThrow().respostas().isEmpty());
        assertThrows(RecursoNaoEncontradoException.class,
                () -> curtidas.definir(topicoId, respostaId, true));
    }
}
