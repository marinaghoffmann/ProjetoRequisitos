package com.g4.game_tracker_backend.infraestrutura.persistencia.similaridade;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.descoberta.similaridade.FeedbackDeSugestao;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.FeedbackSugestaoRepositorio;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.TipoFeedback;

@Repository
@Transactional(readOnly = true)
public class FeedbackSugestaoRepositorioPostgres implements FeedbackSugestaoRepositorio {

    private final FeedbackSugestaoSpringDataRepository springData;

    public FeedbackSugestaoRepositorioPostgres(FeedbackSugestaoSpringDataRepository springData) {
        this.springData = springData;
    }

    /** Grava na hora, para a recontagem logo em seguida (na mesma transação) já enxergar o feedback. */
    @Override
    @Transactional
    public FeedbackDeSugestao salvar(FeedbackDeSugestao feedback) {
        springData.saveAndFlush(FeedbackSugestaoJpa.deDominio(feedback));
        return feedback;
    }

    @Override
    public Optional<FeedbackDeSugestao> buscarPorUsuarioESimilaridade(UUID usuarioId, UUID similaridadeId) {
        return springData.findByUsuarioIdAndSimilaridadeId(usuarioId, similaridadeId)
                .map(FeedbackSugestaoJpa::paraDominio);
    }

    @Override
    public boolean existePorUsuarioESimilaridade(UUID usuarioId, UUID similaridadeId) {
        return springData.existsByUsuarioIdAndSimilaridadeId(usuarioId, similaridadeId);
    }

    @Override
    public int contar(UUID similaridadeId, TipoFeedback tipo) {
        return (int) springData.countBySimilaridadeIdAndTipo(similaridadeId, tipo);
    }

    @Override
    public List<FeedbackDeSugestao> listarDoUsuario(UUID usuarioId, Collection<UUID> similaridadeIds) {
        if (similaridadeIds.isEmpty()) {
            return List.of();
        }
        return springData.findByUsuarioIdAndSimilaridadeIdIn(usuarioId, similaridadeIds).stream()
                .map(FeedbackSugestaoJpa::paraDominio)
                .toList();
    }

    @Override
    @Transactional
    public void remover(UUID id) {
        springData.deleteById(id);
        springData.flush();
    }
}
