package com.g4.game_tracker_backend.dominio.avaliacao.reviews;

import java.util.Objects;
import java.util.UUID;

/** Nota de uma categoria dentro da review. Só existe junto com ela (RN 3: removida em cascata). */
public record NotaPorCategoria(UUID id, CategoriaReview categoria, NotaAvaliacao nota) {

    public NotaPorCategoria {
        Objects.requireNonNull(id, "O identificador da nota é obrigatório");
        Objects.requireNonNull(categoria, "A categoria da nota é obrigatória");
        Objects.requireNonNull(nota, "A nota é obrigatória");
    }

    public NotaPorCategoria comNota(NotaAvaliacao novaNota) {
        return new NotaPorCategoria(id, categoria, novaNota);
    }
}
