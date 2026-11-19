package com.g4.game_tracker_backend.aplicacao.forum;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForumRepositorio;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

/** F-13: qualquer usuário autenticado cria um tópico geral ou atrelado a um jogo ATIVO do catálogo. */
@CasoDeUso
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

    public TopicoDeForum executar(UUID jogoId, String categoria, String titulo, String conteudo) {
        return executar(jogoId, categoria, titulo, conteudo, null);
    }

    @Transacional
    public TopicoDeForum executar(UUID jogoId, String categoria, String titulo, String conteudo,
            List<String> imagens) {
        UUID autorId = verificadorDeAcesso.usuarioAtualId();
        if (jogoId != null && jogos.buscarPorId(jogoId)
                .filter(jogo -> jogo.status() == StatusCatalogo.ATIVO).isEmpty()) {
            throw new RegraDeNegocioException("O jogo precisa estar cadastrado no catálogo");
        }
        return topicos.salvar(TopicoDeForum.criar(autorId, jogoId, categoria, titulo, conteudo, imagens, Instant.now()));
    }
}