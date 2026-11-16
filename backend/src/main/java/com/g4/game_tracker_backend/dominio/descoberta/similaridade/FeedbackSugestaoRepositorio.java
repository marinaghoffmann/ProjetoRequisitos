package com.g4.game_tracker_backend.dominio.descoberta.similaridade;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackSugestaoRepositorio {
    FeedbackDeSugestao salvar(FeedbackDeSugestao feedback);
    Optional<FeedbackDeSugestao> buscarPorUsuarioESimilaridade(UUID usuarioId, UUID similaridadeId);
    boolean existePorUsuarioESimilaridade(UUID usuarioId, UUID similaridadeId);
    int contar(UUID similaridadeId, TipoFeedback tipo);
    /** Feedbacks do usuário para as sugestões informadas (para a tela mostrar o estado dos botões). */
    List<FeedbackDeSugestao> listarDoUsuario(UUID usuarioId, Collection<UUID> similaridadeIds);
    void remover(UUID id);
}
