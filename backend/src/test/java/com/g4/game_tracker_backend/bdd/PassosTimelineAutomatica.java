package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.avaliacao.reviews.PublicarReview;
import com.g4.game_tracker_backend.aplicacao.journal.ConsultarTimeline;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ReviewRepositorio;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournalRepositorio;
import com.g4.game_tracker_backend.dominio.journal.TipoEventoPlataforma;
import com.g4.game_tracker_backend.dominio.journal.TipoOrigem;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/**
 * Steps da F-16. A review é publicada pelo caso de uso real da F-02. Conclusão e pedido vêm de
 * AcoesDeApoioDaTimeline, que publica os eventos de plataforma dentro de uma transação, no lugar de F-11 e
 * F-20. O journal e a timeline são reais.
 */
public class PassosTimelineAutomatica {

    private final AcoesDeApoioDaTimeline acoes;
    private final PublicarReview publicarReview;
    private final ReviewRepositorio reviews;
    private final ConsultarTimeline consultarTimeline;
    private final EntradaDeJournalRepositorio entradas;
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;

    private boolean gravacaoDaReviewVaiFalhar;
    private String reviewPublicada;
    private String conclusaoRegistrada;
    private EntradaDeJournal entradaAutomatica;
    private List<EntradaDeJournal> timelineConsultada;

    public PassosTimelineAutomatica(AcoesDeApoioDaTimeline acoes, PublicarReview publicarReview,
            ReviewRepositorio reviews, ConsultarTimeline consultarTimeline, EntradaDeJournalRepositorio entradas,
            DadosDoCenario dados, ContextoDoCenario contexto) {
        this.acoes = acoes;
        this.publicarReview = publicarReview;
        this.reviews = reviews;
        this.consultarTimeline = consultarTimeline;
        this.entradas = entradas;
        this.dados = dados;
        this.contexto = contexto;
    }

    // ---- Ações na plataforma (publicam os eventos) ----

    @Dado("que a gravação da review vai falhar")
    public void gravacaoDaReviewVaiFalhar() {
        gravacaoDaReviewVaiFalhar = true;
    }

    @Quando("o jogador publica uma review de {string}")
    public void jogadorPublicaReview(String titulo) {
        UUID jogoId = dados.jogo(titulo).id();
        if (gravacaoDaReviewVaiFalhar) {
            assertThrows(AcoesDeApoioDaTimeline.FalhaSimulada.class,
                    () -> acoes.executarEFalharDepois(() -> reviewPublicada = publicarReview(jogoId)));
        } else {
            reviewPublicada = publicarReview(jogoId);
        }
    }

    @Quando("o jogador registra uma conclusão de {string}")
    public void jogadorRegistraConclusao(String titulo) {
        conclusaoRegistrada = acoes.registrarConclusao(jogador(), dados.jogo(titulo).id(), Instant.now());
    }

    @Quando("o jogador confirma o recebimento de um pedido de {string}")
    public void jogadorConfirmaRecebimento(String titulo) {
        acoes.confirmarRecebimento(jogador(), dados.jogo(titulo).id(), Instant.now());
    }

    @Dado("que o jogador registrou uma conclusão de {string} que gerou entrada automática")
    public void jogadorRegistrouConclusao(String titulo) {
        jogadorRegistraConclusao(titulo);
        assertEquals(1, automaticas("JOGO_ZERADO").size());
    }

    @Quando("o jogador exclui o registro de conclusão de {string}")
    public void jogadorExcluiConclusao(String titulo) {
        acoes.excluirConclusao(conclusaoRegistrada);
    }

    // ---- Edição (RN 3) ----

    @Dado("que o jogador possui uma entrada automática do tipo {string}")
    public void jogadorPossuiEntradaAutomatica(String tipo) {
        UUID jogoId = dados.jogo("Hades").id();
        String origemId = switch (TipoEventoPlataforma.valueOf(tipo)) {
            case REVIEW_PUBLICADA -> publicarReview(jogoId);
            case JOGO_ZERADO -> acoes.registrarConclusao(jogador(), jogoId, Instant.now());
            case COMPRA_CONCLUIDA -> acoes.confirmarRecebimento(jogador(), jogoId, Instant.now());
        };
        entradaAutomatica = entradas.buscarPorOrigem(TipoEventoPlataforma.valueOf(tipo), origemId).orElseThrow();
    }

    /** A F-15 vai expor a edição por um caso de uso; a regra está no agregado, que ele precisa chamar. */
    @Quando("o jogador tenta alterar o texto da entrada automática")
    public void jogadorTentaAlterarTexto() {
        contexto.executar(() -> entradas.salvar(entradaAutomatica.editar(jogador(), "Texto alterado à mão", false,
                Instant.now())));
    }

    // ---- Timeline ----

    @Dado("que o jogador possui {int} entradas manuais e {int} entradas automáticas")
    public void jogadorPossuiEntradas(int manuais, int automaticas) {
        Instant agora = Instant.now();
        for (int i = 0; i < manuais; i++) {
            Instant sessao = agora.minus(Duration.ofDays(2L * i));
            entradas.salvar(EntradaDeJournal.criarManual(jogador(), null, sessao, "Sessão manual " + (i + 1), false,
                    agora));
        }
        for (int i = 0; i < automaticas; i++) {
            Instant ocorrido = agora.minus(Duration.ofDays(2L * i + 1));
            acoes.registrarConclusao(jogador(), dados.jogo("Jogo " + (i + 1)).id(), ocorrido);
        }
    }

    @Quando("o jogador consulta a sua timeline")
    public void jogadorConsultaTimeline() {
        timelineConsultada = consultarTimeline.consultar(1, ConsultarTimeline.TAMANHO_MAXIMO_DA_PAGINA).itens();
    }

    // ---- Verificações ----

    @Entao("^o journal deve ter (\\d+) entradas? automáticas?(?: do tipo \"([^\"]*)\")?$")
    public void journalDeveTerEntradasAutomaticas(int quantidade, String tipo) {
        assertEquals(quantidade, automaticas(tipo).size());
    }

    @Entao("a review não deve ser registrada")
    public void reviewNaoDeveSerRegistrada() {
        assertNotNull(reviewPublicada, "O caso de uso da review não chegou a rodar");
        assertTrue(reviews.buscarPorId(UUID.fromString(reviewPublicada)).isEmpty(),
                "A review foi gravada apesar da falha");
        assertTrue(entradas.buscarPorOrigem(TipoEventoPlataforma.REVIEW_PUBLICADA, reviewPublicada).isEmpty(),
                "A entrada automática da review sobreviveu ao rollback");
    }

    @Entao("a timeline deve conter {int} entradas ordenadas por data")
    public void timelineDeveConterEntradasOrdenadas(int quantidade) {
        assertNotNull(timelineConsultada);
        assertEquals(quantidade, timelineConsultada.size());
        assertEquals(timelineConsultada.stream()
                .sorted(Comparator.comparing(EntradaDeJournal::dataSessao).reversed()).toList(), timelineConsultada);
        assertTrue(timelineConsultada.stream().anyMatch(entrada -> entrada.tipoOrigem() == TipoOrigem.MANUAL));
        assertTrue(timelineConsultada.stream().anyMatch(entrada -> entrada.tipoOrigem() == TipoOrigem.AUTOMATICO));
    }

    // ---- Apoio ----

    /** Publica pelo caso de uso da F-02, que publica o ReviewPublicada dentro da sua transação. */
    private String publicarReview(UUID jogoId) {
        return publicarReview.executar(jogoId, 5, List.of(), "Review da timeline").id().toString();
    }

    /** Lê pela consulta real da timeline do jogador autenticado. */
    private List<EntradaDeJournal> automaticas(String tipo) {
        return consultarTimeline.consultar(1, ConsultarTimeline.TAMANHO_MAXIMO_DA_PAGINA).itens().stream()
                .filter(EntradaDeJournal::automatica)
                .filter(entrada -> tipo == null || entrada.tipoEvento() == TipoEventoPlataforma.valueOf(tipo))
                .toList();
    }

    private UUID jogador() {
        return dados.jogadorAtual().id();
    }
}
