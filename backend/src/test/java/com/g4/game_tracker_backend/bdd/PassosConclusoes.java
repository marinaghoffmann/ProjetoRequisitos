package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.biblioteca.conclusoes.GerenciarConclusoes;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogoRepositorio;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusao;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusao.Dados;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournalRepositorio;
import com.g4.game_tracker_backend.dominio.journal.TipoEventoPlataforma;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

public class PassosConclusoes {

    private static final LocalDate DATA_PADRAO = LocalDate.of(2024, 1, 10);
    private static final Dados CONCLUSAO_PADRAO = new Dados(DATA_PADRAO, "PC", null, true, false);
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;
    private final GerenciarConclusoes conclusoes;
    private final EstatisticaJogoRepositorio estatisticas;
    private final EntradaDeJournalRepositorio journal;

    private RegistroDeConclusao registro;
    private List<RegistroDeConclusao> historico;

    public PassosConclusoes(DadosDoCenario dados, ContextoDoCenario contexto,
            GerenciarConclusoes conclusoes, EstatisticaJogoRepositorio estatisticas,
            EntradaDeJournalRepositorio journal) {
        this.dados = dados;
        this.contexto = contexto;
        this.conclusoes = conclusoes;
        this.estatisticas = estatisticas;
        this.journal = journal;
    }

    @Dado("^que o jogador zerou \"([^\"]*)\" (\\d+) vez(?:es)?$")
    public void jogadorZerouVezes(String titulo, int total) {
        for (int i = 0; i < total; i++) {
            conclusoes.registrar(jogo(titulo), CONCLUSAO_PADRAO);
        }
    }

    @Dado("que o jogador zerou {string} em {string}, {string} e {string}")
    public void jogadorZerouNasDatas(String titulo, String primeira, String segunda, String terceira) {
        for (String data : List.of(primeira, segunda, terceira)) {
            conclusoes.registrar(jogo(titulo), new Dados(LocalDate.parse(data), null, null, true, false));
        }
    }

    @Dado("que o jogador possui um registro de conclusão de {string} na plataforma {string}")
    public void possuiRegistro(String titulo, String plataforma) {
        registro = conclusoes.registrar(jogo(titulo), new Dados(DATA_PADRAO, plataforma, null, true, false));
    }

    @Dado("que o jogador {string} possui um registro de conclusão de {string}")
    public void outroPossuiRegistro(String jogador, String titulo) {
        var atual = dados.jogadorAtual();
        SessaoDeTeste.autenticarComo(dados.jogador(jogador));
        try {
            registro = conclusoes.registrar(jogo(titulo), CONCLUSAO_PADRAO);
        } finally {
            SessaoDeTeste.autenticarComo(atual);
        }
    }

    @Quando("o jogador registra uma conclusão de {string} em {string} na plataforma {string} com {int} horas")
    public void registraCompleto(String titulo, String data, String plataforma, int horas) {
        registrar(titulo, new Dados(LocalDate.parse(data), plataforma, BigDecimal.valueOf(horas), true, false));
    }

    @Quando("^o jogador registra uma conclusão de \"([^\"]*)\" em \"([^\"]*)\" com (-?\\d+(?:\\.\\d+)?) horas$")
    public void registraComHoras(String titulo, String data, String horas) {
        registrar(titulo, new Dados(LocalDate.parse(data), null, new BigDecimal(horas), true, false));
    }

    @Quando("o jogador registra uma conclusão de {string} em {string} sem informar horas")
    public void registraSemHoras(String titulo, String data) {
        registrar(titulo, new Dados(LocalDate.parse(data), null, null, true, false));
    }

    @Quando("o jogador registra uma conclusão de {string} com data futura")
    public void registraNoFuturo(String titulo) {
        registrar(titulo, new Dados(LocalDate.now().plusDays(1), null, null, true, false));
    }

    @Quando("o jogador registra uma conclusão de {string} sem data")
    public void registraSemData(String titulo) {
        registrar(titulo, new Dados(null, null, null, true, false));
    }

    @Quando("o jogador registra {string} com platina marcada e conclusão principal desmarcada")
    public void registraPlatinaSemConclusao(String titulo) {
        registrar(titulo, new Dados(DATA_PADRAO, null, null, false, true));
    }

    @Quando("o jogador exclui um dos seus registros de conclusão de {string}")
    public void excluiRegistroProprio(String titulo) {
        UUID id = conclusoes.meuHistorico(jogo(titulo)).getFirst().id();
        contexto.executar(() -> conclusoes.excluir(id));
    }

    @Quando("o jogador {string} tenta editar o registro de {string}")
    public void tentaEditarRegistroDeOutro(String solicitante, String dono) {
        dados.autenticar(solicitante);
        editarPlataforma("Nintendo Switch");
    }

    @Quando("o jogador {string} tenta excluir a conclusão de {string}")
    public void tentaExcluirRegistroDeOutro(String solicitante, String dono) {
        dados.autenticar(solicitante);
        contexto.executar(() -> conclusoes.excluir(registro.id()));
    }

    @Quando("o jogador altera a plataforma do registro para {string}")
    public void editarPlataforma(String plataforma) {
        contexto.executar(() -> conclusoes.editar(registro.id(), new Dados(registro.dataConclusao(),
                plataforma, registro.horasJogadas(), registro.concluido(), registro.platina())));
    }

    @Quando("o jogador troca o jogo do registro para {string}")
    public void trocaJogoDoRegistro(String titulo) {
        contexto.executar(() -> conclusoes.editar(registro.id(), jogo(titulo), CONCLUSAO_PADRAO));
    }

    @Entao("o mesmo registro deve aparecer em Zerados para {string} e na timeline desse jogo")
    public void confereJogoDoRegistroETimeline(String titulo) {
        UUID jogoId = jogo(titulo);
        var linha = conclusoes.meusRegistros().stream().filter(item -> item.id().equals(registro.id()))
                .findFirst().orElseThrow();
        assertEquals(jogoId, linha.jogoId());
        assertEquals(dados.jogo(titulo).titulo(), linha.titulo());
        assertEquals("PC", linha.plataforma());
        var entrada = journal.buscarPorOrigem(TipoEventoPlataforma.JOGO_ZERADO, registro.id().toString()).orElseThrow();
        assertEquals(jogoId, entrada.jogoId());
    }

    @Quando("o jogador consulta o histórico de conclusões de {string}")
    public void consultaHistorico(String titulo) {
        historico = contexto.obter(() -> conclusoes.meuHistorico(jogo(titulo)));
    }

    @Entao("o contador de conclusões de {string} do jogador deve ser {int}")
    public void confereContador(String titulo, int total) {
        assertNull(contexto.erro());
        assertEquals(total, conclusoes.meuHistorico(jogo(titulo)).size());
    }

    @Entao("a ficha de estatísticas de {string} deve registrar {int} conclusões")
    public void confereEstatisticas(String titulo, int total) {
        assertEquals(total, estatisticas.buscarPorJogo(jogo(titulo)).orElseThrow().totalConclusoes());
    }

    @Entao("{string} deve aparecer uma única vez em Zerados com {int} conclusões")
    public void confereResumoDeZerados(String titulo, int total) {
        var jogoId = jogo(titulo);
        var resumos = conclusoes.meusJogosZerados().stream()
                .filter(resumo -> resumo.jogoId().equals(jogoId)).toList();
        assertEquals(1, resumos.size());
        assertEquals(total, resumos.getFirst().totalConclusoes());
    }

    @Entao("{string} não deve aparecer nos meus jogos zerados")
    public void jogoNaoApareceEmZerados(String titulo) {
        var jogoId = jogo(titulo);
        assertEquals(0, conclusoes.meusJogosZerados().stream()
                .filter(resumo -> resumo.jogoId().equals(jogoId)).count());
        assertEquals(0, conclusoes.meusRegistros().stream()
                .filter(item -> item.jogoId().equals(jogoId)).count());
    }

    @Entao("o registro deve ter a plataforma {string}")
    public void conferePlataforma(String plataforma) {
        assertNull(contexto.erro());
        var editado = conclusoes.meuHistorico(registro.jogoId()).getFirst();
        assertEquals(registro.id(), editado.id());
        assertEquals(plataforma, editado.plataforma());
    }

    @Entao("o registro deve ser aceito")
    public void confereRegistroAceito() {
        assertNull(contexto.erro());
        assertNotNull(registro);
    }

    @Entao("o registro deve ser recusado")
    public void confereRegistroRecusado() {
        assertNotNull(contexto.erro());
        assertNull(registro);
    }

    @Entao("as conclusões devem aparecer na ordem {string}, {string}, {string}")
    public void confereOrdem(String primeira, String segunda, String terceira) {
        assertEquals(List.of(primeira, segunda, terceira),
                historico.stream().map(item -> item.dataConclusao().toString()).toList());
    }

    private void registrar(String titulo, Dados entrada) {
        registro = contexto.obter(() -> conclusoes.registrar(jogo(titulo), entrada));
    }

    private UUID jogo(String titulo) {
        return dados.jogo(titulo).id();
    }
}
