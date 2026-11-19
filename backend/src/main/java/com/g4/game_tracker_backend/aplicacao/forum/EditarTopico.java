package com.g4.game_tracker_backend.aplicacao.forum;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.forum.StatusTopico;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForumRepositorio;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

/**
 * F-13 RN 1 e RN 2: o autor edita (em até 48 horas), muda o status ou exclui o seu tópico,
 * desde que ele não esteja encerrado. A exclusão é lógica.
 */
@Service
public class EditarTopico {

    private final TopicoDeForumRepositorio topicos;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final EstadoPublico moderacao;

    public EditarTopico(TopicoDeForumRepositorio topicos, VerificadorDeAcesso verificadorDeAcesso,
            EstadoPublico moderacao) {
        this.topicos = topicos;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.moderacao = moderacao;
    }

    public TopicoDeForum executar(UUID id, String titulo, String conteudo, String categoria) {
        return executar(id, titulo, conteudo, categoria, null);
    }

    /** imagens nulo mantém as imagens atuais do tópico; uma lista substitui todas. */
    @Transactional
    public TopicoDeForum executar(UUID id, String titulo, String conteudo, String categoria,
            List<String> imagens) {
        TopicoDeForum atual = TopicosDoForum.visivelParaAlteracao(topicos, id);
        moderacao.exigirAtivo(TipoConteudo.TOPICO,id);
        return topicos.salvar(atual.editar(verificadorDeAcesso.usuarioAtualId(), titulo, conteudo, categoria,
                imagens, Instant.now()));
    }

    @Transactional
    public TopicoDeForum alterarStatus(UUID id, StatusTopico status) {
        TopicoDeForum atual = TopicosDoForum.visivelParaAlteracao(topicos, id);
        moderacao.exigirAtivo(TipoConteudo.TOPICO,id);
        return topicos.salvar(atual.alterarStatus(verificadorDeAcesso.usuarioAtualId(), status, Instant.now()));
    }

    @Transactional
    public void excluir(UUID id) {
        TopicoDeForum atual = TopicosDoForum.visivelParaAlteracao(topicos, id);
        moderacao.exigirAtivo(TipoConteudo.TOPICO,id);
        topicos.salvar(atual.remover(verificadorDeAcesso.usuarioAtualId()));
    }
}