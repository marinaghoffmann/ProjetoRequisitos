package com.g4.game_tracker_backend.forum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForumRepositorio;

@SpringBootTest
@ActiveProfiles("test")
class ForumConcorrenciaTest {

    @Autowired
    private TopicoDeForumRepositorio topicos;

    @Autowired
    private PlatformTransactionManager transacoes;

    @Test
    void duasRespostasSimultaneasPermanecemNaArvoreENoContador() throws Exception {
        UUID autor = UUID.randomUUID();
        UUID topicoId = topicos.salvar(TopicoDeForum.criar(autor, null, "Geral",
                "Concorrência no fórum", "Tópico de teste", Instant.now())).id();
        CountDownLatch inicio = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<?> primeira = executor.submit(() -> responderDepoisDoSinal(inicio, topicoId, "Primeira"));
            Future<?> segunda = executor.submit(() -> responderDepoisDoSinal(inicio, topicoId, "Segunda"));
            inicio.countDown();
            primeira.get(20, TimeUnit.SECONDS);
            segunda.get(20, TimeUnit.SECONDS);
        }
        TopicoDeForum salvo = topicos.buscarPorId(topicoId).orElseThrow();
        assertEquals(2, salvo.totalRespostas());
        assertEquals(2, salvo.respostas().size());
        assertTrue(salvo.respostas().stream().anyMatch(resposta -> resposta.conteudo().equals("Primeira")));
        assertTrue(salvo.respostas().stream().anyMatch(resposta -> resposta.conteudo().equals("Segunda")));
    }

    private void responderDepoisDoSinal(CountDownLatch inicio, UUID topicoId, String conteudo) {
        try {
            if (!inicio.await(20, TimeUnit.SECONDS)) {
                throw new AssertionError("As escritas não começaram");
            }
            new TransactionTemplate(transacoes).executeWithoutResult(estado -> {
                TopicoDeForum atual = topicos.buscarPorIdParaAlteracao(topicoId).orElseThrow();
                topicos.salvar(atual.responder(UUID.randomUUID(), null, conteudo, Instant.now()));
            });
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(erro);
        }
    }
}
