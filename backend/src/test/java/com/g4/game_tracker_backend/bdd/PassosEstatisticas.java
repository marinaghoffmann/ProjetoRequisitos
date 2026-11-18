package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;

import com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas.ConsultarEstatisticas;
import com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas.RecalcularEstatisticas;
import com.g4.game_tracker_backend.aplicacao.avaliacao.reviews.GerenciarReview;
import com.g4.game_tracker_backend.aplicacao.avaliacao.reviews.PublicarReview;
import com.g4.game_tracker_backend.aplicacao.biblioteca.conclusoes.GerenciarConclusoes;
import com.g4.game_tracker_backend.aplicacao.wishlist.AdicionarNaWishlist;
import com.g4.game_tracker_backend.apresentacao.avaliacao.estatisticas.EstatisticaJogoResponse;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogoRepositorio;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.Review;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusao.Dados;
import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.EstadoConservacao;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.MidiaJogo;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.MudancaDeStatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/**
 * Steps da F-17. Reviews e conclusões publicam métricas pelos casos de uso reais.
 * A wishlist também usa o caso de uso real da F-06, cada inclusão com um jogador diferente.
 */
public class PassosEstatisticas {

    private static final String ESTUDIO_DE_APOIO = "Estúdio BDD";
    private static final int NOTA_DE_APOIO = 4;

    private final RecalcularEstatisticas recalcular;
    private final ConsultarEstatisticas consultar;
    private final EstatisticaJogoRepositorio estatisticas;
    private final AdicionarNaWishlist adicionarNaWishlist;
    private final AnuncioMarketplaceRepositorio anuncios;
    private final JogoRepositorio jogos;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final ApplicationEventPublisher eventos;
    private final PublicarReview publicarReview;
    private final GerenciarReview gerenciarReview;
    private final GerenciarConclusoes conclusoes;
    private final DadosDoCenario dados;

    private final Map<String, Jogo> jogosDoCenario = new HashMap<>();
    private final List<Review> reviewsDoCenario = new ArrayList<>();
    private final Map<UUID, Usuario> autoresDasReviews = new HashMap<>();
    private Jogo jogoAtual;
    private EstatisticaJogoResponse fichaConsultada;

    public PassosEstatisticas(RecalcularEstatisticas recalcular, ConsultarEstatisticas consultar,
            EstatisticaJogoRepositorio estatisticas, AdicionarNaWishlist adicionarNaWishlist,
            AnuncioMarketplaceRepositorio anuncios, JogoRepositorio jogos,
            DesenvolvedoraRepositorio desenvolvedoras, ApplicationEventPublisher eventos,
            PublicarReview publicarReview, GerenciarReview gerenciarReview, GerenciarConclusoes conclusoes,
            DadosDoCenario dados) {
        this.recalcular = recalcular;
        this.consultar = consultar;
        this.estatisticas = estatisticas;
        this.adicionarNaWishlist = adicionarNaWishlist;
        this.anuncios = anuncios;
        this.jogos = jogos;
        this.desenvolvedoras = desenvolvedoras;
        this.eventos = eventos;
        this.publicarReview = publicarReview;
        this.gerenciarReview = gerenciarReview;
        this.conclusoes = conclusoes;
        this.dados = dados;
    }

    // ---- Situação inicial ----

    @Dado("^que o jogo \"([^\"]*)\" possui reviews com notas gerais ([\\d, e]+)$")
    public void jogoPossuiReviewsComNotas(String titulo, String notas) {
        UUID jogoId = jogo(titulo).id();
        Arrays.stream(notas.split(",| e ")).map(String::trim).map(Integer::valueOf)
                .forEach(nota -> publicarReview(jogoId, nota));
        recalcular.executar(jogoId);
    }

    @Dado("que o jogo {string} possui {int} reviews ativas")
    public void jogoPossuiReviewsAtivas(String titulo, int quantidade) {
        UUID jogoId = jogo(titulo).id();
        for (int i = 0; i < quantidade; i++) {
            publicarReview(jogoId, NOTA_DE_APOIO);
        }
        recalcular.executar(jogoId);
    }

    @Dado("que o jogo {string} possui {int} conclusões")
    public void jogoPossuiConclusoes(String titulo, int quantidade) {
        UUID jogoId = jogo(titulo).id();
        for (int i = 0; i < quantidade; i++) {
            registrarConclusao(jogoId);
        }
        recalcular.executar(jogoId);
    }

    @Dado("que o jogo {string} está em {int} wishlists")
    public void jogoEstaEmWishlists(String titulo, int quantidade) {
        UUID jogoId = jogo(titulo).id();
        for (int i = 0; i < quantidade; i++) {
            adicionarNaWishlist(jogoId);
        }
        recalcular.executar(jogoId);
    }

    @Dado("que o jogo {string} possui {int} negociações concluídas")
    public void jogoPossuiNegociacoes(String titulo, int quantidade) {
        UUID jogoId = jogo(titulo).id();
        for (int i = 0; i < quantidade; i++) {
            venderAnuncio(jogoId);
        }
        recalcular.executar(jogoId);
    }

    // ---- Ações que disparam o recálculo por evento (RN 1) ----

    @Quando("um jogador publica uma review de {string} com nota geral {int}")
    public void jogadorPublicaReview(String titulo, int nota) {
        publicarReview(jogo(titulo).id(), nota);
    }

    @Quando("a review com nota geral {int} é excluída")
    public void reviewEExcluida(int nota) {
        Review review = reviewsDoCenario.stream()
                .filter(publicada -> publicada.jogoId().equals(jogoAtual.id()))
                .filter(publicada -> publicada.notaGeral().valor() == nota)
                .findFirst()
                .orElseThrow();
        SessaoDeTeste.autenticarComo(autoresDasReviews.get(review.autorId()));
        gerenciarReview.excluir(review.id());
        reviewsDoCenario.remove(review);
    }

    @Quando("um jogador registra uma conclusão de {string}")
    public void jogadorRegistraConclusao(String titulo) {
        registrarConclusao(jogo(titulo).id());
    }

    @Quando("um jogador adiciona {string} à wishlist")
    public void jogadorAdicionaAWishlist(String titulo) {
        adicionarNaWishlist(jogo(titulo).id());
    }

    @Quando("um pedido de {string} é concluído no marketplace")
    public void pedidoEConcluido(String titulo) {
        eventos.publishEvent(venderAnuncio(jogo(titulo).id()).evento());
    }

    @Quando("as estatísticas de {string} são recalculadas")
    public void estatisticasSaoRecalculadas(String titulo) {
        recalcular.executar(jogo(titulo).id());
    }

    @Quando("o usuário consulta a ficha de estatísticas de {string}")
    public void usuarioConsultaFicha(String titulo) {
        fichaConsultada = EstatisticaJogoResponse.de(consultar.consultarFicha(jogo(titulo).id()));
    }

    // ---- Verificações ----

    @Entao("^a média geral de \"([^\"]*)\" deve ser (\\d+\\.\\d{2})$")
    public void mediaGeralDeveSer(String titulo, String media) {
        assertEquals(new BigDecimal(media), gravada(titulo).mediaGeral());
    }

    /** A leitura vem do banco, então também confere a precisão da coluna (RN 2). */
    @Entao("^a média geral de \"([^\"]*)\" deve ser gravada como (\\d+\\.\\d{2})$")
    public void mediaGeralDeveSerGravadaComo(String titulo, String media) {
        assertEquals(new BigDecimal(media), gravada(titulo).mediaGeral());
    }

    @Entao("o total de conclusões de {string} deve ser {int}")
    public void totalDeConclusoesDeveSer(String titulo, int total) {
        assertEquals(total, gravada(titulo).totalConclusoes());
    }

    @Entao("o total de wishlists de {string} deve ser {int}")
    public void totalDeWishlistsDeveSer(String titulo, int total) {
        assertEquals(total, gravada(titulo).totalEmWishlists());
    }

    @Entao("o total de negociações de {string} deve ser {int}")
    public void totalDeNegociacoesDeveSer(String titulo, int total) {
        assertEquals(total, gravada(titulo).totalNegociacoes());
    }

    @Entao("as estatísticas públicas não devem ser exibidas")
    public void estatisticasNaoDevemSerExibidas() {
        assertFalse(fichaConsultada.elegivel());
        assertNull(fichaConsultada.mediaGeral());
        assertNull(fichaConsultada.mediasPorCategoria());
        assertNull(fichaConsultada.totalConclusoes());
        assertNull(fichaConsultada.totalEmWishlists());
        assertNull(fichaConsultada.totalNegociacoes());
    }

    @Entao("as estatísticas públicas devem ser exibidas")
    public void estatisticasDevemSerExibidas() {
        assertTrue(fichaConsultada.elegivel());
        assertNotNull(fichaConsultada.mediaGeral());
        assertNotNull(fichaConsultada.totalConclusoes());
    }

    // ---- Apoio ----

    /** Cada review é de um jogador novo (RN 1 da F-02: uma review por jogador e jogo). */
    private void publicarReview(UUID jogoId, int nota) {
        Usuario autor = dados.jogador("revisor " + UUID.randomUUID());
        autoresDasReviews.put(autor.id(), autor);
        SessaoDeTeste.autenticarComo(autor);
        reviewsDoCenario.add(publicarReview.executar(jogoId, nota, List.of(), null));
    }

    private EstatisticaJogo gravada(String titulo) {
        return estatisticas.buscarPorJogo(jogo(titulo).id()).orElseThrow();
    }

    private void adicionarNaWishlist(UUID jogoId) {
        SessaoDeTeste.autenticarComo(dados.jogador("fã " + UUID.randomUUID()));
        adicionarNaWishlist.executar(jogoId);
    }

    private void registrarConclusao(UUID jogoId) {
        SessaoDeTeste.autenticarComo(dados.jogador("jogador das conclusões"));
        conclusoes.registrar(jogoId, new Dados(LocalDate.now(), null, null, true, false));
    }

    private MudancaDeStatusAnuncio venderAnuncio(UUID jogoId) {
        Instant agora = Instant.now();
        AnuncioMarketplace ativo = AnuncioMarketplace.criar(UUID.randomUUID(), jogoId, "Anúncio de apoio",
                EstadoConservacao.USADO, new BigDecimal("100.00"), TipoNegociacao.VENDA, MidiaJogo.FISICO,
                "Recife - PE", List.of("https://exemplo.com/foto.jpg"), agora);
        MudancaDeStatusAnuncio venda = ativo.mudarStatus(StatusAnuncio.EM_NEGOCIACAO, agora).anuncio()
                .mudarStatus(StatusAnuncio.VENDIDO, agora);
        anuncios.salvar(venda.anuncio());
        return venda;
    }

    private Jogo jogo(String titulo) {
        jogoAtual = jogosDoCenario.computeIfAbsent(titulo, nome -> jogos.salvar(Jogo.criarManual(
                nome + " " + UUID.randomUUID(), null, null, estudioDeApoio().id(), Set.of(), null, null, null)));
        return jogoAtual;
    }

    private Desenvolvedora estudioDeApoio() {
        return desenvolvedoras.buscarPorNome(ESTUDIO_DE_APOIO)
                .orElseGet(() -> desenvolvedoras.salvar(Desenvolvedora.criar(ESTUDIO_DE_APOIO, null, null, null,
                        null)));
    }
}
