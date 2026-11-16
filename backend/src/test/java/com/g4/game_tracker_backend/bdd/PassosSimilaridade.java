package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.descoberta.similaridade.AjustarSimilaridadeManual;
import com.g4.game_tracker_backend.aplicacao.descoberta.similaridade.ConsultarSimilares;
import com.g4.game_tracker_backend.aplicacao.descoberta.similaridade.ConsultarSimilares.Similar;
import com.g4.game_tracker_backend.aplicacao.descoberta.similaridade.GerenciarFeedback;
import com.g4.game_tracker_backend.aplicacao.descoberta.similaridade.RecalcularSimilaridades;
import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Genero;
import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.FeedbackSugestaoRepositorio;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeJogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeRepositorio;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.TipoFeedback;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/**
 * Steps da F-10 (similares e feedback). As ações passam pelos casos de uso. Cada jogo do cenário tem o seu
 * próprio estúdio, e os gêneros e tags "em comum" são exclusivos do cenário, para o cálculo não ligar jogos
 * de outros cenários (o banco H2 é compartilhado). Sugestões de apoio com grau fixo são gravadas direto pelo
 * agregado.
 */
public class PassosSimilaridade {

    private final RecalcularSimilaridades recalcular;
    private final ConsultarSimilares consultarSimilares;
    private final GerenciarFeedback gerenciarFeedback;
    private final AjustarSimilaridadeManual ajustarManual;
    private final SimilaridadeRepositorio similaridades;
    private final FeedbackSugestaoRepositorio feedbacks;
    private final JogoRepositorio jogos;
    private final GeneroRepositorio generos;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;

    private final String sufixo = UUID.randomUUID().toString().substring(0, 8);
    private final Map<String, Jogo> jogosDoCenario = new HashMap<>();
    private SimilaridadeJogo sugestaoAtual;
    private List<Similar> consulta;

    public PassosSimilaridade(RecalcularSimilaridades recalcular, ConsultarSimilares consultarSimilares,
            GerenciarFeedback gerenciarFeedback, AjustarSimilaridadeManual ajustarManual,
            SimilaridadeRepositorio similaridades, FeedbackSugestaoRepositorio feedbacks, JogoRepositorio jogos,
            GeneroRepositorio generos, DesenvolvedoraRepositorio desenvolvedoras, DadosDoCenario dados,
            ContextoDoCenario contexto) {
        this.recalcular = recalcular;
        this.consultarSimilares = consultarSimilares;
        this.gerenciarFeedback = gerenciarFeedback;
        this.ajustarManual = ajustarManual;
        this.similaridades = similaridades;
        this.feedbacks = feedbacks;
        this.jogos = jogos;
        this.generos = generos;
        this.desenvolvedoras = desenvolvedoras;
        this.dados = dados;
        this.contexto = contexto;
    }

    // ---- RN 1: cálculo, simetria e auto-similaridade ----

    @Dado("os jogos {string} e {string} com gêneros e tags em comum")
    public void jogosComGenerosETagsEmComum(String primeiro, String segundo) {
        Genero genero = generos.salvar(Genero.criar("Roguelike " + sufixo));
        String metadados = """
                {"tags":["roguelike-%1$s","acao-%1$s"],"temas":["mitologia-%1$s"]}""".formatted(sufixo);
        criarJogo(primeiro, Set.of(genero.id()), metadados);
        criarJogo(segundo, Set.of(genero.id()), metadados);
    }

    @Quando("o sistema calcula a similaridade de {string}")
    public void sistemaCalculaSimilaridade(String titulo) {
        recalcular.recalcular(jogo(titulo).id());
    }

    @Entao("{string} deve aparecer como similar a {string}")
    public void deveAparecerComoSimilar(String similar, String titulo) {
        assertNotNull(similarDe(titulo, similar), similar + " não aparece como similar a " + titulo);
    }

    @Entao("{string} deve aparecer como similar a {string} com o mesmo grau")
    public void deveAparecerComOMesmoGrau(String similar, String titulo) {
        Similar ida = similarDe(similar, titulo);
        Similar volta = similarDe(titulo, similar);
        assertNotNull(volta, similar + " não aparece como similar a " + titulo);
        assertEquals(ida.similaridade().grauRelevancia(), volta.similaridade().grauRelevancia());
        assertEquals(ida.similaridade().id(), volta.similaridade().id(), "O par deveria ser gravado uma vez só");
    }

    @Entao("{string} não deve aparecer na lista de similares de {string}")
    public void naoDeveAparecer(String similar, String titulo) {
        UUID proprio = jogo(similar).id();
        assertTrue(consultar(titulo).stream().noneMatch(item -> item.jogoId().equals(proprio)));
    }

    // ---- RN 2: feedback ----

    @Dado("que {string} é sugerido como similar a {string}")
    public void eSugeridoComoSimilar(String similar, String titulo) {
        sugestaoAtual = sugestao(titulo, similar, new BigDecimal("0.75"));
    }

    @Quando("o jogador marca a sugestão {string} -> {string} como {string}")
    public void jogadorMarcaSugestao(String titulo, String similar, String tipo) {
        sugestaoAtual = sugestao(titulo, similar, new BigDecimal("0.75"));
        contexto.obter(() -> gerenciarFeedback.registrar(sugestaoAtual.id(), TipoFeedback.valueOf(tipo)));
    }

    @Dado("que o jogador já deu feedback {string} para a sugestão {string} -> {string}")
    public void jogadorJaDeuFeedback(String tipo, String titulo, String similar) {
        sugestaoAtual = sugestao(titulo, similar, new BigDecimal("0.75"));
        gerenciarFeedback.registrar(sugestaoAtual.id(), TipoFeedback.valueOf(tipo));
    }

    @Quando("o jogador altera o feedback para {string}")
    public void jogadorAlteraFeedback(String tipo) {
        contexto.obter(() -> gerenciarFeedback.editar(sugestaoAtual.id(), TipoFeedback.valueOf(tipo)));
        assertEquals(TipoFeedback.valueOf(tipo), feedbacks.buscarPorUsuarioESimilaridade(dados.jogadorAtual().id(),
                sugestaoAtual.id()).orElseThrow().tipo());
    }

    @Quando("o jogador remove o seu feedback")
    public void jogadorRemoveFeedback() {
        contexto.obter(() -> gerenciarFeedback.remover(sugestaoAtual.id()));
    }

    @Entao("a sugestão deve ter {int} feedback {string}")
    public void sugestaoDeveTerFeedback(int quantidade, String tipo) {
        assertNull(contexto.erro(), () -> "O feedback foi recusado: " + contexto.erro().getMessage());
        SimilaridadeJogo gravada = recarregar();
        int total = TipoFeedback.valueOf(tipo) == TipoFeedback.UTIL ? gravada.totalFeedbacksUteis()
                : gravada.totalFeedbacksNegativos();
        assertEquals(quantidade, total);
        assertEquals(quantidade, feedbacks.contar(gravada.id(), TipoFeedback.valueOf(tipo)));
    }

    @Entao("a sugestão deve ter {int} feedbacks do jogador")
    public void sugestaoDeveTerFeedbacksDoJogador(int quantidade) {
        assertNull(contexto.erro(), () -> "A ação foi recusada: " + contexto.erro().getMessage());
        boolean possui = feedbacks.buscarPorUsuarioESimilaridade(dados.jogadorAtual().id(), sugestaoAtual.id())
                .isPresent();
        assertEquals(quantidade, possui ? 1 : 0);
        SimilaridadeJogo gravada = recarregar();
        assertEquals(0, gravada.totalFeedbacksUteis() + gravada.totalFeedbacksNegativos());
    }

    @Dado("^que a sugestão \"([^\"]*)\" -> \"([^\"]*)\" tem grau de relevância (\\d+\\.\\d{2}) e (\\d+) feedbacks \"([^\"]*)\"$")
    public void sugestaoTemGrauEFeedbacks(String titulo, String similar, String grau, int quantidade, String tipo) {
        sugestaoAtual = sugestao(titulo, similar, new BigDecimal(grau));
        for (int i = 1; i <= quantidade; i++) {
            SessaoDeTeste.autenticarComo(dados.jogador("avaliador " + i));
            gerenciarFeedback.registrar(sugestaoAtual.id(), TipoFeedback.valueOf(tipo));
        }
    }

    @Quando("outro jogador marca a sugestão {string} -> {string} como {string}")
    public void outroJogadorMarcaSugestao(String titulo, String similar, String tipo) {
        sugestaoAtual = similaridades.buscarPar(jogo(titulo).id(), jogo(similar).id()).orElseThrow();
        SessaoDeTeste.autenticarComo(dados.jogador("outro jogador"));
        contexto.obter(() -> gerenciarFeedback.registrar(sugestaoAtual.id(), TipoFeedback.valueOf(tipo)));
        assertNull(contexto.erro(), () -> "O feedback foi recusado: " + contexto.erro().getMessage());
    }

    @Entao("^o grau de relevância da sugestão deve ser menor que (\\d+\\.\\d{2})$")
    public void grauDeveSerMenorQue(String grau) {
        assertTrue(recarregar().grauRelevancia().compareTo(new BigDecimal(grau)) < 0,
                "O grau não foi rebaixado: " + recarregar().grauRelevancia());
    }

    @Entao("^o grau de relevância da sugestão deve ser (\\d+\\.\\d{2})$")
    public void grauDeveSer(String grau) {
        assertEquals(new BigDecimal(grau), recarregar().grauRelevancia());
    }

    @Entao("a sugestão deve estar marcada como rebaixada")
    public void sugestaoRebaixada() {
        assertTrue(recarregar().rebaixada());
    }

    // ---- RN 3: override manual ----

    @Quando("^o administrador cria uma similaridade manual entre \"([^\"]*)\" e \"([^\"]*)\" com grau (\\d+\\.\\d{2})$")
    public void administradorCriaManual(String titulo, String similar, String grau) {
        contexto.obter(() -> ajustarManual.criarManual(jogo(titulo).id(), jogo(similar).id(), new BigDecimal(grau)));
    }

    @Quando("o usuário tenta criar uma similaridade manual entre {string} e {string}")
    public void usuarioTentaCriarManual(String titulo, String similar) {
        contexto.obter(() -> ajustarManual.criarManual(jogo(titulo).id(), jogo(similar).id(),
                new BigDecimal("0.50")));
    }

    @Entao("{string} deve aparecer como similar a {string} com origem {string}")
    public void deveAparecerComOrigem(String similar, String titulo, String origem) {
        assertNull(contexto.erro(), () -> "A conexão foi recusada: " + contexto.erro().getMessage());
        Similar encontrado = similarDe(titulo, similar);
        assertNotNull(encontrado, similar + " não aparece como similar a " + titulo);
        assertEquals(origem, encontrado.similaridade().origem().name());
    }

    // ---- Consulta ----

    @Dado("^que \"([^\"]*)\" possui similares \"([^\"]*)\" com grau (\\d+\\.\\d{2}), \"([^\"]*)\" com grau (\\d+\\.\\d{2}) e \"([^\"]*)\" com grau (\\d+\\.\\d{2})$")
    public void possuiSimilares(String titulo, String primeiro, String grauPrimeiro, String segundo,
            String grauSegundo, String terceiro, String grauTerceiro) {
        sugestao(titulo, primeiro, new BigDecimal(grauPrimeiro));
        sugestao(titulo, segundo, new BigDecimal(grauSegundo));
        sugestao(titulo, terceiro, new BigDecimal(grauTerceiro));
    }

    @Quando("o usuário consulta os similares de {string}")
    public void usuarioConsultaSimilares(String titulo) {
        consulta = consultar(titulo);
    }

    @Entao("os similares devem aparecer na ordem {string}, {string}, {string}")
    public void similaresNaOrdem(String primeiro, String segundo, String terceiro) {
        assertNotNull(consulta, "Os similares não foram consultados");
        assertEquals(List.of(jogo(primeiro).id(), jogo(segundo).id(), jogo(terceiro).id()),
                consulta.stream().map(Similar::jogoId).toList());
    }

    // ---- Apoio ----

    private List<Similar> consultar(String titulo) {
        return consultarSimilares.consultar(jogo(titulo).id(), null, ConsultarSimilares.LIMITE_MAXIMO);
    }

    private Similar similarDe(String titulo, String similar) {
        UUID procurado = jogo(similar).id();
        return consultar(titulo).stream().filter(item -> item.jogoId().equals(procurado)).findFirst().orElse(null);
    }

    /** Sugestão automática de apoio com grau fixo; reaproveita o par se ele já existe no cenário. */
    private SimilaridadeJogo sugestao(String titulo, String similar, BigDecimal grau) {
        UUID um = jogo(titulo).id();
        UUID dois = jogo(similar).id();
        return similaridades.buscarPar(um, dois)
                .orElseGet(() -> similaridades.salvar(SimilaridadeJogo.automatica(um, dois, grau, Instant.now())));
    }

    private SimilaridadeJogo recarregar() {
        return similaridades.buscarPorId(sugestaoAtual.id()).orElseThrow();
    }

    private Jogo jogo(String titulo) {
        Jogo existente = jogosDoCenario.get(titulo);
        return existente != null ? existente : criarJogo(titulo, Set.of(), "");
    }

    private Jogo criarJogo(String titulo, Set<UUID> generosIds, String metadados) {
        Desenvolvedora estudio = desenvolvedoras.salvar(Desenvolvedora.criar("Estúdio " + titulo + " "
                + UUID.randomUUID(), null, null, null, null));
        Jogo jogo = jogos.salvar(Jogo.criarManual(titulo, null, null, estudio.id(), generosIds, null, null, null)
                .comMetadados(metadados));
        jogosDoCenario.put(titulo, jogo);
        return jogo;
    }
}
