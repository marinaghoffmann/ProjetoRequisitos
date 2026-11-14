package com.g4.game_tracker_backend.aplicacao.forum;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForumRepositorio;

/** F-13: qualquer usuário autenticado cria um tópico geral ou atrelado a um jogo ATIVO do catálogo. */
@Service
public class CriarTopico {

    private final TopicoDeForumRepositorio topicos;
    private final JogoRepositorio jogos;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public CriarTopico(TopicoDeForumRepositorio topicos, JogoRepositorio jogos,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.topicos = topicos;
        this.jogos = jogos;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public TopicoDeForum executar(UUID jogoId, String categoria, String titulo, String conteudo) {
        UUID autorId = verificadorDeAcesso.usuarioAtualId();
        if (jogoId != null && jogos.buscarPorId(jogoId)
                .filter(jogo -> jogo.status() == StatusCatalogo.ATIVO).isEmpty()) {
            throw new RegraDeNegocioException("O jogo precisa estar cadastrado no catálogo");
        }
        return topicos.salvar(TopicoDeForum.criar(autorId, jogoId, categoria, titulo, conteudo, Instant.now()));
    }
}
