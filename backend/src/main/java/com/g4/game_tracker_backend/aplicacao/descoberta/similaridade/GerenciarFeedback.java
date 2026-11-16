package com.g4.game_tracker_backend.aplicacao.descoberta.similaridade;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.FeedbackDeSugestao;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.FeedbackSugestaoRepositorio;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeJogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeRepositorio;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.TipoFeedback;

/**
 * F-10 RN 2 (FeedbackSugestaoService do modelo): o usuário autenticado registra, edita ou remove o seu
 * feedback sobre uma sugestão. Um por usuário e sugestão ("Você já avaliou esta sugestão").
 *
 * Depois de cada mudança, os totais da sugestão são recontados a partir dos feedbacks gravados, e o
 * agregado decide o rebaixamento (mais de 15 negativos). Assim, editar ou remover um feedback desfaz o
 * rebaixamento quando o total volta a 15 ou menos.
 */
@Service
public class GerenciarFeedback {

    private final SimilaridadeRepositorio similaridades;
    private final FeedbackSugestaoRepositorio feedbacks;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public GerenciarFeedback(SimilaridadeRepositorio similaridades, FeedbackSugestaoRepositorio feedbacks,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.similaridades = similaridades;
        this.feedbacks = feedbacks;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public SimilaridadeJogo registrar(UUID similaridadeId, TipoFeedback tipo) {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        SimilaridadeJogo sugestao = Sugestoes.visivel(similaridades, similaridadeId);
        if (feedbacks.existePorUsuarioESimilaridade(usuarioId, similaridadeId)) {
            throw new RegraDeNegocioException(FeedbackDeSugestao.JA_AVALIADA);
        }
        feedbacks.salvar(FeedbackDeSugestao.registrar(usuarioId, similaridadeId, tipo, Instant.now()));
        return recontar(sugestao);
    }

    @Transactional
    public SimilaridadeJogo editar(UUID similaridadeId, TipoFeedback novoTipo) {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        SimilaridadeJogo sugestao = Sugestoes.visivel(similaridades, similaridadeId);
        FeedbackDeSugestao atual = doUsuario(usuarioId, similaridadeId);
        feedbacks.salvar(atual.editar(usuarioId, novoTipo, Instant.now()));
        return recontar(sugestao);
    }

    @Transactional
    public SimilaridadeJogo remover(UUID similaridadeId) {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        SimilaridadeJogo sugestao = Sugestoes.visivel(similaridades, similaridadeId);
        FeedbackDeSugestao atual = doUsuario(usuarioId, similaridadeId);
        atual.exigirAutor(usuarioId);
        feedbacks.remover(atual.id());
        return recontar(sugestao);
    }

    private FeedbackDeSugestao doUsuario(UUID usuarioId, UUID similaridadeId) {
        return feedbacks.buscarPorUsuarioESimilaridade(usuarioId, similaridadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Você ainda não avaliou esta sugestão"));
    }

    private SimilaridadeJogo recontar(SimilaridadeJogo sugestao) {
        int uteis = feedbacks.contar(sugestao.id(), TipoFeedback.UTIL);
        int negativos = feedbacks.contar(sugestao.id(), TipoFeedback.NAO_RELEVANTE);
        return similaridades.salvar(sugestao.comFeedbacks(uteis, negativos, Instant.now()));
    }
}
