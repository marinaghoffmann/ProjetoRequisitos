package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.jdbc.core.JdbcTemplate;

import com.g4.game_tracker_backend.aplicacao.avaliacao.reviews.ConsultarReviews;
import com.g4.game_tracker_backend.aplicacao.avaliacao.reviews.GerenciarReview;
import com.g4.game_tracker_backend.aplicacao.avaliacao.reviews.PublicarReview;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.CategoriaReview;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.NotaInformada;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.NotaPorCategoria;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ResumoReviewsJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.Review;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ReviewJaPublicadaException;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ReviewRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/**
 * Steps da F-02 (reviews). Todas as ações passam pelos casos de uso, autenticadas como o jogador do step.
 * Os jogos vêm de DadosDoCenario, um por cenário, para as médias não somarem reviews de outros cenários
 * (o "Hades" do Contexto, criado por PassosCatalogo, é compartilhado entre os cenários).
 */
public class PassosReviews {

    private static final String TEXTO_DE_APOIO = "Review de apoio do cenário";

    private final PublicarReview publicarReview;
    private final GerenciarReview gerenciarReview;
    private final ConsultarReviews consultarReviews;
    private final ReviewRepositorio reviews;
    private final JdbcTemplate jdbc;
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;

    private final Map<String, Review> reviewPorJogador = new HashMap<>();
    private Rascunho rascunho;
    private Review reviewAtual;
    private ResumoReviewsJogo consulta;

    /** Review sendo escrita no step "publica ... com notas por categoria", completada pelo texto. */
    private record Rascunho(String titulo, int notaGeral, List<NotaInformada> notas) {
    }

    public PassosReviews(PublicarReview publicarReview, GerenciarReview gerenciarReview,
            ConsultarReviews consultarReviews, ReviewRepositorio reviews, JdbcTemplate jdbc, DadosDoCenario dados,
            ContextoDoCenario contexto) {
        this.publicarReview = publicarReview;
        this.gerenciarReview = gerenciarReview;
        this.consultarReviews = consultarReviews;
        this.reviews = reviews;
        this.jdbc = jdbc;
        this.dados = dados;
        this.contexto = contexto;
    }

    // ---- Publicação ----

    @Quando("o jogador publica uma review de {string} com nota geral {int} e notas por categoria:")
    public void jogadorEscreveReviewComNotas(String titulo, int notaGeral, DataTable tabela) {
        List<NotaInformada> notas = tabela.asMaps().stream()
                .map(linha -> new NotaInformada(CategoriaReview.valueOf(linha.get("categoria")),
                        Integer.valueOf(linha.get("nota"))))
                .toList();
        rascunho = new Rascunho(titulo, notaGeral, notas);
    }

    /** Completa a review do step anterior com o texto e a publica. */
    @Quando("o texto {string}")
    public void oTexto(String texto) {
        assertNotNull(rascunho, "Nenhuma review sendo escrita para receber o texto");
        Rascunho escrita = rascunho;
        rascunho = null;
        reviewAtual = contexto.obter(() -> publicarReview.executar(dados.jogo(escrita.titulo()).id(),
                escrita.notaGeral(), escrita.notas(), texto));
    }

    @Quando("o jogador publica uma review de {string} com nota geral {int}")
    public void jogadorPublicaReviewComNotaGeral(String titulo, int notaGeral) {
        reviewAtual = contexto.obter(() -> publicarReview.executar(dados.jogo(titulo).id(), notaGeral, List.of(),
                TEXTO_DE_APOIO));
    }

    @Entao("a review deve ser registrada com {int} notas por categoria")
    public void reviewDeveSerRegistrada(int quantidade) {
        assertNotNull(reviewAtual, () -> "A review foi recusada: " + contexto.erro().getMessage());
        Review gravada = reviews.buscarPorId(reviewAtual.id()).orElseThrow();
        assertEquals(quantidade, gravada.notasPorCategoria().size());
        assertEquals(dados.jogadorAtual().id(), gravada.autorId());
        assertEquals(reviewAtual.texto(), gravada.texto());
        assertTrue(!gravada.texto().isBlank(), "O texto da review não foi gravado");
    }

    // ---- RN 1: uma review por usuário e jogo ----

    @Dado("que o jogador {string} já possui uma review de {string}")
    public void jogadorJaPossuiReview(String nome, String titulo) {
        publicarComo(nome, titulo, 4, List.of());
    }

    @Quando("o jogador tenta publicar uma nova review de {string}")
    public void jogadorTentaPublicarNovaReview(String titulo) {
        reviewAtual = contexto.obter(() -> publicarReview.executar(dados.jogo(titulo).id(), 5, List.of(),
                "Tentando avaliar de novo"));
    }

    @Entao("nenhuma nova review deve ser criada")
    public void nenhumaNovaReviewCriada() {
        assertNull(reviewAtual, "Uma nova review foi criada");
        List<Review> doJogador = reviews.listarPorAutor(dados.jogadorAtual().id());
        assertEquals(1, doJogador.size());
        assertEquals(reviewPorJogador.get(dados.jogadorAtual().nome()).id(), doJogador.getFirst().id());
    }

    @Entao("o sistema deve indicar o redirecionamento para a edição da review existente")
    public void sistemaIndicaRedirecionamento() {
        ReviewJaPublicadaException erro = assertInstanceOf(ReviewJaPublicadaException.class, contexto.erro());
        assertEquals(reviewPorJogador.get(dados.jogadorAtual().nome()).id(), erro.reviewExistenteId());
    }

    // ---- RN 2: só o autor altera ----

    @Dado("que o jogador {string} já possui uma review de {string} com nota geral {int}")
    public void jogadorJaPossuiReviewComNota(String nome, String titulo, int notaGeral) {
        publicarComo(nome, titulo, notaGeral, List.of());
    }

    @Dado("que o jogador {string} possui uma review de {string}")
    public void jogadorPossuiReview(String nome, String titulo) {
        publicarComo(nome, titulo, 3, List.of());
    }

    @Quando("o jogador {string} altera a nota geral da sua review para {int}")
    public void jogadorAlteraNotaGeral(String nome, int novaNota) {
        Review sua = reviewPorJogador.get(nome);
        reviewAtual = como(nome, () -> contexto.obter(() -> gerenciarReview.editar(sua.id(), novaNota,
                notasInformadas(sua), sua.texto())));
    }

    @Entao("a review deve ter nota geral {int}")
    public void reviewDeveTerNotaGeral(int nota) {
        assertNotNull(reviewAtual, () -> "A edição foi recusada: " + contexto.erro().getMessage());
        assertEquals(nota, reviews.buscarPorId(reviewAtual.id()).orElseThrow().notaGeral().valor());
    }

    @Quando("o jogador {string} tenta editar a review de {string}")
    public void jogadorTentaEditarReviewDeOutro(String nome, String autor) {
        Review alheia = reviewPorJogador.get(autor);
        como(nome, () -> contexto.obter(() -> gerenciarReview.editar(alheia.id(), 1, List.of(), "Editada por outro")));
        assertEquals(alheia.notaGeral(), reviews.buscarPorId(alheia.id()).orElseThrow().notaGeral());
    }

    @Quando("o jogador {string} tenta excluir a review de {string}")
    public void jogadorTentaExcluirReviewDeOutro(String nome, String autor) {
        Review alheia = reviewPorJogador.get(autor);
        como(nome, () -> {
            contexto.executar(() -> gerenciarReview.excluir(alheia.id()));
            return null;
        });
        assertTrue(reviews.buscarPorId(alheia.id()).isPresent(), "A review de outro jogador foi excluída");
    }

    // ---- RN 3: exclusão em cascata ----

    @Dado("que o jogador {string} já possui uma review de {string} com {int} notas por categoria")
    public void jogadorPossuiReviewComNotasPorCategoria(String nome, String titulo, int quantidade) {
        List<NotaInformada> notas = Arrays.stream(CategoriaReview.values())
                .limit(quantidade)
                .map(categoria -> new NotaInformada(categoria, 4))
                .toList();
        Review review = publicarComo(nome, titulo, 4, notas);
        assertEquals(quantidade, notasGravadas(review.id()));
    }

    @Quando("o jogador {string} exclui a sua review de {string}")
    public void jogadorExcluiSuaReview(String nome, String titulo) {
        reviewAtual = reviewPorJogador.get(nome);
        assertEquals(dados.jogo(titulo).id(), reviewAtual.jogoId());
        como(nome, () -> {
            contexto.executar(() -> gerenciarReview.excluir(reviewAtual.id()));
            return null;
        });
    }

    @Entao("a review não deve mais existir")
    public void reviewNaoDeveMaisExistir() {
        assertNull(contexto.erro(), () -> "A exclusão foi recusada: " + contexto.erro().getMessage());
        assertTrue(reviews.buscarPorId(reviewAtual.id()).isEmpty(), "A review continua gravada");
    }

    /** Confere direto na tabela: a exclusão é física, e não lógica. */
    @Entao("não deve existir nenhuma nota por categoria vinculada a ela")
    public void naoDeveExistirNotaVinculada() {
        assertEquals(0, notasGravadas(reviewAtual.id()));
    }

    // ---- Consulta ----

    @Dado("que o jogo {string} possui reviews com nota de {string} {int} e {int}")
    public void jogoPossuiReviewsComNotaDaCategoria(String titulo, String categoria, int primeira, int segunda) {
        CategoriaReview avaliada = CategoriaReview.valueOf(categoria);
        publicarComo("avaliador 1", titulo, 4, List.of(new NotaInformada(avaliada, primeira)));
        publicarComo("avaliador 2", titulo, 4, List.of(new NotaInformada(avaliada, segunda)));
    }

    @Quando("o usuário consulta as reviews de {string}")
    public void usuarioConsultaReviews(String titulo) {
        consulta = consultarReviews.porJogo(dados.jogo(titulo).id(), 1, 10);
    }

    @Entao("^a média de \"([^\"]*)\" deve ser (\\d+\\.\\d{2})$")
    public void mediaDaCategoriaDeveSer(String categoria, String media) {
        assertNotNull(consulta, "As reviews não foram consultadas");
        assertEquals(new BigDecimal(media), consulta.mediaDe(CategoriaReview.valueOf(categoria)));
    }

    // ---- Apoio ----

    /** Publica pelo caso de uso, autenticado como o jogador, e volta à sessão do jogador do cenário. */
    private Review publicarComo(String nome, String titulo, int notaGeral, List<NotaInformada> notas) {
        Review review = como(nome, () -> publicarReview.executar(dados.jogo(titulo).id(), notaGeral, notas,
                TEXTO_DE_APOIO));
        reviewPorJogador.put(nome, review);
        return review;
    }

    private <T> T como(String nome, Supplier<T> acao) {
        Usuario jogador = dados.jogador(nome);
        SessaoDeTeste.autenticarComo(jogador);
        try {
            return acao.get();
        } finally {
            if (dados.jogadorAtual() != null) {
                SessaoDeTeste.autenticarComo(dados.jogadorAtual());
            }
        }
    }

    private long notasGravadas(UUID reviewId) {
        return jdbc.queryForObject("select count(*) from reviews_notas_categoria where review_id = ?", Long.class,
                reviewId);
    }

    private static List<NotaInformada> notasInformadas(Review review) {
        return review.notasPorCategoria().stream()
                .map((NotaPorCategoria nota) -> new NotaInformada(nota.categoria(), nota.nota().valor()))
                .toList();
    }
}
