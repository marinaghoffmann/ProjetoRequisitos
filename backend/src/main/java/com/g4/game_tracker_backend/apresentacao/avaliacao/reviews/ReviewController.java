package com.g4.game_tracker_backend.apresentacao.avaliacao.reviews;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.avaliacao.reviews.ConsultarReviews;
import com.g4.game_tracker_backend.aplicacao.avaliacao.reviews.ConsultarReviews.JogoAvaliado;
import com.g4.game_tracker_backend.aplicacao.avaliacao.reviews.GerenciarReview;
import com.g4.game_tracker_backend.aplicacao.avaliacao.reviews.PublicarReview;
import com.g4.game_tracker_backend.apresentacao.compartilhado.PaginaResponse;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ResumoReviewsJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.Review;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ReviewJaPublicadaException;

import jakarta.validation.Valid;

/** F-02: reviews com nota geral, notas por categoria e texto. */
@RestController
@RequestMapping("/api/avaliacao")
public class ReviewController {

    private final PublicarReview publicarReview;
    private final GerenciarReview gerenciarReview;
    private final ConsultarReviews consultarReviews;

    public ReviewController(PublicarReview publicarReview, GerenciarReview gerenciarReview,
            ConsultarReviews consultarReviews) {
        this.publicarReview = publicarReview;
        this.gerenciarReview = gerenciarReview;
        this.consultarReviews = consultarReviews;
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> publicar(@Valid @RequestBody ReviewRequest request) {
        Review review = publicarReview.executar(request.jogoId(), request.notaGeral(), request.notasInformadas(),
                request.texto());
        return ResponseEntity.created(URI.create("/api/avaliacao/reviews/" + review.id())).body(detalhar(review));
    }

    @PutMapping("/reviews/{id}")
    public ReviewResponse editar(@PathVariable UUID id, @Valid @RequestBody ReviewRequest request) {
        return detalhar(gerenciarReview.editar(id, request.notaGeral(), request.notasInformadas(),
                request.texto()));
    }

    /** Exclusão física: a review e as notas por categoria deixam de existir. */
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        gerenciarReview.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews/minhas")
    public List<ReviewResponse> minhas() {
        return detalhar(consultarReviews.doUsuarioAtual());
    }

    @GetMapping("/jogos/{jogoId}/reviews")
    public ReviewsDoJogoResponse doJogo(@PathVariable UUID jogoId,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        ResumoReviewsJogo resumo = consultarReviews.porJogo(jogoId, pagina, tamanho);
        Map<UUID, String> nomes = consultarReviews.nomesDosAutores(
                resumo.reviews().itens().stream().map(Review::autorId).toList());
        return ReviewsDoJogoResponse.de(resumo,
                PaginaResponse.de(resumo.reviews().map(review -> ReviewResponse.de(review, nomes, Map.of()))));
    }

    /** A review do usuário autenticado para o jogo (200) ou nenhuma (204). */
    @GetMapping("/jogos/{jogoId}/reviews/minha")
    public ResponseEntity<ReviewResponse> minhaDoJogo(@PathVariable UUID jogoId) {
        return consultarReviews.doUsuarioAtualParaOJogo(jogoId)
                .map(review -> ResponseEntity.ok(detalhar(review)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * RN 1: o usuário já avaliou o jogo. 409 com o id da review existente, para a tela abrir a edição.
     * Tratado aqui, e não no TratadorGlobalDeErros, porque só esta funcionalidade devolve o campo extra.
     */
    @ExceptionHandler(ReviewJaPublicadaException.class)
    public ResponseEntity<ProblemDetail> reviewJaPublicada(ReviewJaPublicadaException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problema.setTitle("Review já publicada");
        problema.setProperty("reviewExistenteId", e.reviewExistenteId());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problema);
    }

    private ReviewResponse detalhar(Review review) {
        return detalhar(List.of(review)).getFirst();
    }

    private List<ReviewResponse> detalhar(Collection<Review> reviews) {
        Map<UUID, String> nomes = consultarReviews.nomesDosAutores(reviews.stream().map(Review::autorId).toList());
        Map<UUID, JogoAvaliado> jogos = consultarReviews.jogosAvaliados(
                reviews.stream().map(Review::jogoId).toList());
        return reviews.stream().map(review -> ReviewResponse.de(review, nomes, jogos)).toList();
    }
}
