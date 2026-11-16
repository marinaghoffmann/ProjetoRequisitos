package com.g4.game_tracker_backend.infraestrutura.persistencia.reviews;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.avaliacao.reviews.CategoriaReview;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ResumoDeNotas;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.Review;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ReviewRepositorio;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.SomaDeNotas;
import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;

@Repository
public class ReviewRepositorioPostgres implements ReviewRepositorio {

    private static final Sort MAIS_RECENTES_PRIMEIRO = Sort.by(Sort.Order.desc("criadaEm"), Sort.Order.desc("id"));

    private final ReviewSpringDataRepository springData;

    public ReviewRepositorioPostgres(ReviewSpringDataRepository springData) {
        this.springData = springData;
    }

    /** A review existente é alterada no lugar, para as notas por categoria manterem a identidade. */
    @Override
    public Review salvar(Review review) {
        ReviewJpa jpa = springData.findById(review.id())
                .map(existente -> {
                    existente.atualizarCom(review);
                    return existente;
                })
                .orElseGet(() -> ReviewJpa.deDominio(review));
        springData.save(jpa);
        return review;
    }

    @Override
    public Optional<Review> buscarPorId(UUID id) {
        return springData.findById(id).map(ReviewJpa::paraDominio);
    }

    @Override
    public Optional<Review> buscarPorAutorEJogo(UUID autorId, UUID jogoId) {
        return springData.findByAutorIdAndJogoId(autorId, jogoId).map(ReviewJpa::paraDominio);
    }

    @Override
    public Pagina<Review> listarPorJogo(UUID jogoId, int pagina, int tamanho) {
        Page<ReviewJpa> resultado = springData.findByJogoId(jogoId,
                PageRequest.of(pagina - 1, tamanho, MAIS_RECENTES_PRIMEIRO));
        return new Pagina<>(resultado.map(ReviewJpa::paraDominio).getContent(), pagina, tamanho,
                resultado.getTotalElements());
    }

    @Override
    public List<Review> listarPorAutor(UUID autorId) {
        return springData.findByAutorIdOrderByCriadaEmDesc(autorId).stream().map(ReviewJpa::paraDominio).toList();
    }

    @Override
    public ResumoDeNotas resumirNotas(UUID jogoId) {
        SomaDeNotas geral = springData.somarNotasGerais(jogoId).stream()
                .findFirst()
                .map(linha -> new SomaDeNotas(numero(linha[0]), (int) numero(linha[1])))
                .orElseGet(SomaDeNotas::vazia);
        Map<CategoriaReview, SomaDeNotas> porCategoria = new EnumMap<>(CategoriaReview.class);
        for (Object[] linha : springData.somarNotasPorCategoria(jogoId)) {
            porCategoria.put((CategoriaReview) linha[0], new SomaDeNotas(numero(linha[1]), (int) numero(linha[2])));
        }
        return new ResumoDeNotas(geral, porCategoria);
    }

    /** RN 3: exclusão física; as notas por categoria vão junto. */
    @Override
    public void remover(UUID id) {
        springData.deleteById(id);
    }

    private static long numero(Object valor) {
        return valor == null ? 0 : ((Number) valor).longValue();
    }
}
