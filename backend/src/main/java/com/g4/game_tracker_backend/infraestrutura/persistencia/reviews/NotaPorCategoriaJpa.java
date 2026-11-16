package com.g4.game_tracker_backend.infraestrutura.persistencia.reviews;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.avaliacao.reviews.CategoriaReview;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.NotaAvaliacao;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.NotaPorCategoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Nota de uma categoria. A coluna review_id é preenchida pela review (dona da associação) e aparece aqui
 * só para leitura e para a restrição única: cada categoria uma vez por review.
 */
@Entity
@Table(name = "reviews_notas_categoria", uniqueConstraints = @UniqueConstraint(
        name = "uk_reviews_notas_review_categoria", columnNames = { "review_id", "categoria" }))
public class NotaPorCategoriaJpa {

    @Id
    private UUID id;

    @Column(name = "review_id", nullable = false, insertable = false, updatable = false)
    private UUID reviewId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CategoriaReview categoria;

    @Column(nullable = false)
    private int nota;

    protected NotaPorCategoriaJpa() {
    }

    private NotaPorCategoriaJpa(NotaPorCategoria nota) {
        this.id = nota.id();
        this.categoria = nota.categoria();
        this.nota = nota.nota().valor();
    }

    static NotaPorCategoriaJpa deDominio(NotaPorCategoria nota) {
        return new NotaPorCategoriaJpa(nota);
    }

    NotaPorCategoria paraDominio() {
        return new NotaPorCategoria(id, categoria, new NotaAvaliacao(nota));
    }

    CategoriaReview categoria() {
        return categoria;
    }

    void alterarNota(int novaNota) {
        this.nota = novaNota;
    }
}
