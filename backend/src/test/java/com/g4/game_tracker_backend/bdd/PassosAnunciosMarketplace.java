package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.CodificadorDeSenha;
import com.g4.game_tracker_backend.aplicacao.marketplace.anuncios.AlterarStatusAnuncio;
import com.g4.game_tracker_backend.aplicacao.marketplace.anuncios.ConsultarAnuncios;
import com.g4.game_tracker_backend.aplicacao.marketplace.anuncios.CriarAnuncio;
import com.g4.game_tracker_backend.aplicacao.marketplace.anuncios.EditarAnuncio;
import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.EstadoConservacao;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.FiltroAnuncios;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.MidiaJogo;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;

import io.cucumber.java.ParameterType;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/**
 * Steps da F-18 (anúncios do marketplace). O banco H2 é compartilhado entre os cenários, por isso
 * cada cenário cria os seus próprios jogos de apoio e filtra as buscas por eles.
 */
public class PassosAnunciosMarketplace {

    private static final String ESTUDIO_DE_APOIO = "Estúdio BDD";
    private static final String LOCALIDADE = "Recife - PE";

    private final CriarAnuncio criarAnuncio;
    private final EditarAnuncio editarAnuncio;
    private final AlterarStatusAnuncio alterarStatusAnuncio;
    private final ConsultarAnuncios consultarAnuncios;
    private final AnuncioMarketplaceRepositorio anuncios;
    private final JogoRepositorio jogos;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final UsuarioRepositorio usuarios;
    private final CodificadorDeSenha codificadorDeSenha;
    private final ContextoDoCenario contexto;

    private final Map<String, Usuario> jogadores = new HashMap<>();
    private final Map<String, Jogo> jogosDoCenario = new HashMap<>();
    private Usuario jogadorAtual;
    private AnuncioMarketplace anuncioAtual;
    private Jogo jogoDaBusca;
    private List<AnuncioMarketplace> resultadoDaBusca;

    public PassosAnunciosMarketplace(CriarAnuncio criarAnuncio, EditarAnuncio editarAnuncio,
            AlterarStatusAnuncio alterarStatusAnuncio, ConsultarAnuncios consultarAnuncios,
            AnuncioMarketplaceRepositorio anuncios, JogoRepositorio jogos, DesenvolvedoraRepositorio desenvolvedoras,
            UsuarioRepositorio usuarios, CodificadorDeSenha codificadorDeSenha, ContextoDoCenario contexto) {
        this.criarAnuncio = criarAnuncio;
        this.editarAnuncio = editarAnuncio;
        this.alterarStatusAnuncio = alterarStatusAnuncio;
        this.consultarAnuncios = consultarAnuncios;
        this.anuncios = anuncios;
        this.jogos = jogos;
        this.desenvolvedoras = desenvolvedoras;
        this.usuarios = usuarios;
        this.codificadorDeSenha = codificadorDeSenha;
        this.contexto = contexto;
    }

    /** Valores em reais escritos como nos cenários: "200,00" ou "1.250,90". */
    @ParameterType("\\d{1,3}(?:\\.\\d{3})*,\\d{2}")
    public BigDecimal reais(String valor) {
        return new BigDecimal(valor.replace(".", "").replace(',', '.'));
    }

    /** Valores livres das tabelas de exemplos: "59.90", "0", "-10.00". */
    @ParameterType("-?\\d+(?:\\.\\d+)?")
    public BigDecimal valor(String valor) {
        return new BigDecimal(valor);
    }

    // ---- Jogadores ----

    @Dado("o jogador {string} autenticado")
    public void jogadorAutenticado(String nome) {
        jogadorAtual = jogador(nome);
        SessaoDeTeste.autenticarComo(jogadorAtual);
    }

    // ---- RN 1: vínculo com o catálogo e fotos ----

    @Quando("o jogador anuncia {string} usado por R$ {reais} para {string} com {int} foto(s)")
    public void jogadorAnunciaUsado(String titulo, BigDecimal preco, String tipo, int fotos) {
        anunciar(jogo(titulo).id(), EstadoConservacao.USADO, preco, TipoNegociacao.valueOf(tipo), fotos(fotos));
    }

    @Quando("o jogador anuncia {string} por R$ {reais} sem fotos")
    public void jogadorAnunciaSemFotos(String titulo, BigDecimal preco) {
        anunciar(jogo(titulo).id(), EstadoConservacao.USADO, preco, TipoNegociacao.VENDA, List.of());
    }

    @Quando("o jogador anuncia um jogo que não existe no catálogo")
    public void jogadorAnunciaJogoInexistente() {
        anunciar(UUID.randomUUID(), EstadoConservacao.USADO, new BigDecimal("100.00"), TipoNegociacao.VENDA,
                fotos(1));
    }

    @Quando("o jogador anuncia {string} por {valor} com {int} foto(s)")
    public void jogadorAnunciaPorValor(String titulo, BigDecimal preco, int fotos) {
        anunciar(jogo(titulo).id(), EstadoConservacao.USADO, preco, TipoNegociacao.VENDA, fotos(fotos));
    }

    @Entao("o anúncio deve ser criado com status {string}")
    public void anuncioCriadoComStatus(String status) {
        assertNotNull(anuncioAtual, () -> "O anúncio foi recusado: " + contexto.erro().getMessage());
        assertEquals(StatusAnuncio.valueOf(status), anuncioAtual.status());
    }

    @Entao("o anúncio deve ser criado")
    public void anuncioCriado() {
        assertNull(contexto.erro(), () -> "O anúncio foi recusado: " + contexto.erro().getMessage());
        assertNotNull(anuncioAtual);
    }

    @Entao("o anúncio deve ser recusado")
    public void anuncioRecusado() {
        assertNull(anuncioAtual, "Esperava que o anúncio fosse recusado, mas ele foi criado");
        assertNotNull(contexto.erro());
    }

    // ---- RN 2: ciclo de vida e propriedade ----

    @Dado("que o jogador possui um anúncio com status {string}")
    public void jogadorPossuiAnuncio(String status) {
        anuncioAtual = anuncioDe(jogadorAtual, jogo("Anúncio de apoio"), EstadoConservacao.USADO,
                new BigDecimal("120.00"), StatusAnuncio.valueOf(status));
    }

    @Dado("que o jogador {string} possui um anúncio com status {string}")
    public void outroJogadorPossuiAnuncio(String nome, String status) {
        anuncioAtual = anuncioDe(jogador(nome), jogo("Anúncio de apoio"), EstadoConservacao.USADO,
                new BigDecimal("120.00"), StatusAnuncio.valueOf(status));
    }

    @Quando("o jogador pausa o anúncio")
    public void jogadorPausaAnuncio() {
        anuncioAtual = contexto.obter(() -> alterarStatusAnuncio.executar(anuncioAtual.id(), StatusAnuncio.PAUSADO));
    }

    @Quando("o jogador {string} tenta alterar o preço do anúncio de {string}")
    public void jogadorTentaAlterarPreco(String nome, String dono) {
        SessaoDeTeste.autenticarComo(jogador(nome));
        UUID anuncioDoOutro = anuncioAtual.id();
        contexto.obter(() -> editarAnuncio.alterarPreco(anuncioDoOutro, new BigDecimal("1.00")));
    }

    @Entao("o anúncio deve ter status {string}")
    public void anuncioDeveTerStatus(String status) {
        assertNotNull(anuncioAtual, () -> "A ação foi recusada: " + contexto.erro().getMessage());
        assertEquals(StatusAnuncio.valueOf(status), anuncios.buscarPorId(anuncioAtual.id()).orElseThrow().status());
    }

    // ---- RN 3 e consulta ----

    @Dado("que existem {int} anúncios de {string} com status {string} e {int} com status {string}")
    public void existemAnunciosComStatus(int qtdA, String titulo, String statusA, int qtdB, String statusB) {
        Jogo jogo = jogo(titulo);
        Usuario vendedor = jogador("vendedor de apoio");
        for (int i = 0; i < qtdA; i++) {
            anuncioDe(vendedor, jogo, EstadoConservacao.USADO, new BigDecimal("90.00"), StatusAnuncio.valueOf(statusA));
        }
        for (int i = 0; i < qtdB; i++) {
            anuncioDe(vendedor, jogo, EstadoConservacao.USADO, new BigDecimal("90.00"), StatusAnuncio.valueOf(statusB));
        }
    }

    @Dado("anúncios de {string} {string} por R$ {reais}, {string} por R$ {reais} e {string} por R$ {reais}")
    public void anunciosComEstadoEPreco(String titulo, String estadoA, BigDecimal precoA, String estadoB,
            BigDecimal precoB, String estadoC, BigDecimal precoC) {
        Jogo jogo = jogo(titulo);
        Usuario vendedor = jogador("vendedor de apoio");
        anuncioDe(vendedor, jogo, EstadoConservacao.valueOf(estadoA), precoA, StatusAnuncio.ATIVO);
        anuncioDe(vendedor, jogo, EstadoConservacao.valueOf(estadoB), precoB, StatusAnuncio.ATIVO);
        anuncioDe(vendedor, jogo, EstadoConservacao.valueOf(estadoC), precoC, StatusAnuncio.ATIVO);
    }

    @Quando("um usuário busca anúncios de {string}")
    public void usuarioBuscaAnunciosDoJogo(String titulo) {
        resultadoDaBusca = contexto.obter(() -> consultarAnuncios.buscar(
                new FiltroAnuncios(jogo(titulo).id(), null, null, null, null, null, null)));
    }

    @Quando("um usuário busca anúncios {string} entre R$ {reais} e R$ {reais}")
    public void usuarioBuscaPorEstadoEPreco(String estado, BigDecimal minimo, BigDecimal maximo) {
        resultadoDaBusca = contexto.obter(() -> consultarAnuncios.buscar(new FiltroAnuncios(jogoDaBusca.id(), null,
                minimo, maximo, EstadoConservacao.valueOf(estado), null, null)));
    }

    @Entao("a busca deve retornar {int} anúncio(s)")
    public void buscaDeveRetornar(int quantidade) {
        assertNotNull(resultadoDaBusca, () -> "A busca foi recusada: " + contexto.erro().getMessage());
        assertEquals(quantidade, resultadoDaBusca.size());
    }

    // ---- Apoio ----

    private void anunciar(UUID jogoId, EstadoConservacao estado, BigDecimal preco, TipoNegociacao tipo,
            List<String> fotos) {
        anuncioAtual = contexto.obter(() -> criarAnuncio.executar(jogoId, null, estado, preco, tipo,
                MidiaJogo.FISICO, LOCALIDADE, fotos));
    }

    /** Cria o anúncio direto no repositório, levando-o ao status pedido pelas transições do contrato. */
    private AnuncioMarketplace anuncioDe(Usuario vendedor, Jogo jogo, EstadoConservacao estado, BigDecimal preco,
            StatusAnuncio status) {
        AnuncioMarketplace anuncio = AnuncioMarketplace.criar(vendedor.id(), jogo.id(), jogo.titulo(), estado, preco,
                TipoNegociacao.VENDA, MidiaJogo.FISICO, LOCALIDADE, fotos(1), Instant.now());
        for (StatusAnuncio passo : caminhoAte(status)) {
            anuncio = anuncio.mudarStatus(passo, Instant.now()).anuncio();
        }
        return anuncios.salvar(anuncio);
    }

    private static List<StatusAnuncio> caminhoAte(StatusAnuncio status) {
        return switch (status) {
            case ATIVO -> List.of();
            case PAUSADO -> List.of(StatusAnuncio.PAUSADO);
            case CANCELADO -> List.of(StatusAnuncio.CANCELADO);
            case EM_NEGOCIACAO -> List.of(StatusAnuncio.EM_NEGOCIACAO);
            case VENDIDO -> List.of(StatusAnuncio.EM_NEGOCIACAO, StatusAnuncio.VENDIDO);
        };
    }

    private static List<String> fotos(int quantidade) {
        return Collections.nCopies(quantidade, "/uploads/catalogo/foto-bdd.jpg");
    }

    /** Um jogo novo por nome em cada cenário, para as buscas não contarem anúncios de outros cenários. */
    private Jogo jogo(String titulo) {
        Jogo jogo = jogosDoCenario.computeIfAbsent(titulo, nome -> jogos.salvar(Jogo.criarManual(
                nome + " " + UUID.randomUUID(), null, null, estudioDeApoio().id(), Set.of(), null, null, null)));
        jogoDaBusca = jogo;
        return jogo;
    }

    private Desenvolvedora estudioDeApoio() {
        return desenvolvedoras.buscarPorNome(ESTUDIO_DE_APOIO)
                .orElseGet(() -> desenvolvedoras.salvar(Desenvolvedora.criar(ESTUDIO_DE_APOIO, null, null, null,
                        null)));
    }

    private Usuario jogador(String nome) {
        return jogadores.computeIfAbsent(nome, apelido -> usuarios.salvar(Usuario.reconstituir(UUID.randomUUID(),
                apelido, UUID.randomUUID() + "@bdd.example", codificadorDeSenha.codificar("senha-segura-123"),
                Papel.COMUM, true, null)));
    }
}
