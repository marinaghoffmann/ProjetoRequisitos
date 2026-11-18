package com.g4.game_tracker_backend.dominio.avaliacao.reviews;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;

public interface ReviewRepositorio {
    Review salvar(Review review);
    Optional<Review> buscarPorId(UUID id);
    Optional<Review> buscarPorAutorEJogo(UUID autorId, UUID jogoId);
    /** Reviews do jogo, da mais recente para a mais antiga. A página começa em 1. */
    Pagina<Review> listarPorJogo(UUID jogoId, int pagina, int tamanho);
    /** Reviews do autor, da mais recente para a mais antiga. */
    List<Review> listarPorAutor(UUID autorId);
    /** Somas das notas gerais e por categoria de todas as reviews do jogo. */
    ResumoDeNotas resumirNotas(UUID jogoId);
    /** Exclusão física: a review e as suas notas por categoria (RN 3). */
    void remover(UUID id);
    boolean existeParaJogo(UUID jogoId);
}
