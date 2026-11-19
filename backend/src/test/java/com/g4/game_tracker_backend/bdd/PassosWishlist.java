package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import com.g4.game_tracker_backend.aplicacao.wishlist.AdicionarNaWishlist;
import com.g4.game_tracker_backend.aplicacao.wishlist.ConsultarWishlist;
import com.g4.game_tracker_backend.aplicacao.wishlist.ItemWishlistDetalhado;
import com.g4.game_tracker_backend.aplicacao.wishlist.RemoverDaWishlist;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.wishlist.PrioridadeWishlist;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/**
 * Steps da F-06. As ações passam pelos casos de uso reais com o jogador autenticado de
 * {@link DadosDoCenario}; as recusas ficam no {@link ContextoDoCenario} e são conferidas pelo step comum.
 */
public class PassosWishlist {

    private final AdicionarNaWishlist adicionar;
    private final RemoverDaWishlist remover;
    private final ConsultarWishlist consultar;
    private final WishlistRepositorio wishlists;
    private final JdbcTemplate jdbc;
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;

    private List<ItemWishlistDetalhado> consultada;

    public PassosWishlist(AdicionarNaWishlist adicionar, RemoverDaWishlist remover, ConsultarWishlist consultar,
            WishlistRepositorio wishlists, JdbcTemplate jdbc, DadosDoCenario dados, ContextoDoCenario contexto) {
        this.adicionar = adicionar;
        this.remover = remover;
        this.consultar = consultar;
        this.wishlists = wishlists;
        this.jdbc = jdbc;
        this.dados = dados;
        this.contexto = contexto;
    }

    // ---- Dado ----

    @Dado("que o jogador ainda não possui wishlist")
    public void jogadorAindaNaoPossuiWishlist() {
        assertTrue(wishlists.buscarPorDono(dados.jogadorAtual().id()).isEmpty());
    }

    @Dado("que o jogador já possui uma wishlist com o jogo {string}")
    public void jogadorJaPossuiWishlistComJogo(String jogo) {
        adicionar.executar(idDo(jogo), null);
    }

    @Dado("que o jogador {string} possui na wishlist {string} com prioridade {string}, {string} com prioridade {string} e {string} com prioridade {string}")
    public void outroJogadorPossuiNaWishlist(String nome, String jogoA, String prioridadeA, String jogoB,
            String prioridadeB, String jogoC, String prioridadeC) {
        Usuario atual = dados.jogadorAtual();
        SessaoDeTeste.autenticarComo(dados.jogador(nome));
        try {
            adicionar.executar(idDo(jogoA), PrioridadeWishlist.valueOf(prioridadeA));
            adicionar.executar(idDo(jogoB), PrioridadeWishlist.valueOf(prioridadeB));
            adicionar.executar(idDo(jogoC), PrioridadeWishlist.valueOf(prioridadeC));
        } finally {
            SessaoDeTeste.autenticarComo(atual);
        }
    }

    // ---- Quando ----

    @Quando("o jogador adiciona o jogo {string} à wishlist com prioridade {string}")
    public void jogadorAdicionaComPrioridade(String jogo, String prioridade) {
        contexto.executar(() -> adicionar.executar(idDo(jogo), PrioridadeWishlist.valueOf(prioridade)));
    }

    @Quando("o jogador adiciona o jogo {string} à wishlist sem informar prioridade")
    public void jogadorAdicionaSemPrioridade(String jogo) {
        contexto.executar(() -> adicionar.executar(idDo(jogo), null));
    }

    @Quando("o jogador remove o jogo {string} da wishlist")
    public void jogadorRemove(String jogo) {
        contexto.executar(() -> remover.executar(idDo(jogo)));
    }

    @Quando("o jogador consulta a wishlist de {string}")
    public void jogadorConsultaWishlistDe(String nome) {
        consultada = contexto.obter(() -> consultar.executar(dados.jogador(nome).id()));
    }

    // ---- Então ----

    @Entao("o jogador deve possuir exatamente {int} wishlist")
    public void jogadorDevePossuirWishlists(int quantidade) {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM wishlists WHERE dono_id = ?", Integer.class,
                dados.jogadorAtual().id());
        assertEquals(quantidade, total);
    }

    @Entao("a wishlist deve conter o jogo {string} com prioridade {string}")
    public void wishlistDeveConterJogoComPrioridade(String jogo, String prioridade) {
        UUID jogoId = idDo(jogo);
        ItemWishlistDetalhado item = consultar.doUsuarioAtual().stream()
                .filter(i -> i.jogo().id().equals(jogoId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("O jogo " + jogo + " não está na wishlist"));
        assertEquals(PrioridadeWishlist.valueOf(prioridade), item.prioridade());
    }

    @Entao("a wishlist deve conter {int} jogos")
    public void wishlistDeveConterJogos(int quantidade) {
        assertEquals(quantidade, consultar.doUsuarioAtual().size());
    }

    @Entao("os jogos devem aparecer na ordem {string}, {string}, {string}")
    public void jogosDevemAparecerNaOrdem(String primeiro, String segundo, String terceiro) {
        assertEquals(List.of(idDo(primeiro), idDo(segundo), idDo(terceiro)),
                consultada.stream().map(i -> i.jogo().id()).toList());
    }

    private UUID idDo(String jogo) {
        return dados.jogo(jogo).id();
    }
}
