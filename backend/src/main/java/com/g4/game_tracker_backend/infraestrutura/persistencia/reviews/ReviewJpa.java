package com.g4.game_tracker_backend.infraestrutura.persistencia.reviews;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.g4.game_tracker_backend.dominio.avaliacao.reviews.CategoriaReview;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.NotaPorCategoria;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.Review;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Review com as notas por categoria.
 *
 * RN 1: a restrição única (autor_id, jogo_id) impede duas reviews do mesmo usuário para o mesmo jogo,
 * mesmo em requisições simultâneas.
 * RN 3: as notas saem junto com a review, pelo JPA (cascade e orphanRemoval) e pelo banco (FK com
 * ON DELETE CASCADE).
 */
@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(name = "uk_reviews_autor_jogo",
        columnNames = { "autor_id", "jogo_id" }), indexes = @Index(name = "ix_reviews_jogo", columnList = "jogo_id"))
public class ReviewJpa {

    @Id
    private UUID id;

    @Column(name = "autor_id", nullable = false)
    private UUID autorId;

    @Column(name = "jogo_id", nullable = false)
    private UUID jogoId;

    @Column(name = "nota_geral", nullable = false)
    private int notaGeral;

    @Column(nullable = false, length = Review.LIMITE_DO_TEXTO)
    private String texto;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @BatchSize(size = 50)
    private List<NotaPorCategoriaJpa> notas = new ArrayList<>();

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private Instant atualizadaEm;

    protected ReviewJpa() {
    }

    private ReviewJpa(Review review) {
        this.id = review.id();
        this.autorId = review.autorId();
        this.jogoId = review.jogoId();
        this.criadaEm = review.criadaEm();
        atualizarCom(review);
    }

    static ReviewJpa deDominio(Review review) {
        return new ReviewJpa(review);
    }

    /**
     * Atualiza a entidade gerenciada no lugar: as notas de categorias que continuam são alteradas, as que
     * saem são removidas (orphanRemoval) e as novas são inseridas.
     */
    void atualizarCom(Review review) {
        this.notaGeral = review.notaGeral().valor();
        this.texto = review.texto();
        this.atualizadaEm = review.atualizadaEm();

        Map<CategoriaReview, NotaPorCategoria> novas = new EnumMap<>(CategoriaReview.class);
        review.notasPorCategoria().forEach(nota -> novas.put(nota.categoria(), nota));
        notas.removeIf(nota -> !novas.containsKey(nota.categoria()));
        for (NotaPorCategoria nova : novas.values()) {
            notas.stream()
                    .filter(nota -> nota.categoria() == nova.categoria())
                    .findFirst()
                    .ifPresentOrElse(nota -> nota.alterarNota(nova.nota().valor()),
                            () -> notas.add(NotaPorCategoriaJpa.deDominio(nova)));
        }
    }

    Review paraDominio() {
        return Review.reconstituir(id, autorId, jogoId, notaGeral, texto,
                notas.stream().map(NotaPorCategoriaJpa::paraDominio).toList(), criadaEm, atualizadaEm);
    }
}
