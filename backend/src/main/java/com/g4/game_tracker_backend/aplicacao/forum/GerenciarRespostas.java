package com.g4.game_tracker_backend.aplicacao.forum;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.forum.RespostaForum;
import com.g4.game_tracker_backend.dominio.forum.RespostaPublicada;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForumRepositorio;

/**
 * F-13: publicar, editar e excluir respostas. As regras (tópico encerrado, autor, prazo de 48 horas,
 * contador e última atividade) ficam no agregado TopicoDeForum. Cada resposta nova publica um
 * RespostaPublicada no contexto do Spring, até o barramento da T04 existir.
 */
@Service
public class GerenciarRespostas {

    private final TopicoDeForumRepositorio topicos;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final ApplicationEventPublisher eventos;

    public GerenciarRespostas(TopicoDeForumRepositorio topicos, VerificadorDeAcesso verificadorDeAcesso,
            ApplicationEventPublisher eventos) {
        this.topicos = topicos;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.eventos = eventos;
    }

    @Transactional
    public RespostaForum responder(UUID topicoId, UUID respostaPaiId, String conteudo) {
        TopicoDeForum atual = TopicosDoForum.visivel(topicos, topicoId);
        TopicoDeForum atualizado = atual.responder(verificadorDeAcesso.usuarioAtualId(), respostaPaiId, conteudo,
                Instant.now());
        topicos.salvar(atualizado);
        RespostaForum nova = atualizado.ultimaResposta();
        eventos.publishEvent(new RespostaPublicada(topicoId, nova.id(), nova.autorId(), nova.publicadaEm()));
        return nova;
    }

    @Transactional
    public RespostaForum editar(UUID topicoId, UUID respostaId, String conteudo) {
        TopicoDeForum atual = TopicosDoForum.visivel(topicos, topicoId);
        TopicoDeForum atualizado = atual.editarResposta(respostaId, verificadorDeAcesso.usuarioAtualId(), conteudo,
                Instant.now());
        return topicos.salvar(atualizado).resposta(respostaId);
    }

    @Transactional
    public void excluir(UUID topicoId, UUID respostaId) {
        TopicoDeForum atual = TopicosDoForum.visivel(topicos, topicoId);
        topicos.salvar(atual.excluirResposta(respostaId, verificadorDeAcesso.usuarioAtualId(), Instant.now()));
    }
}
