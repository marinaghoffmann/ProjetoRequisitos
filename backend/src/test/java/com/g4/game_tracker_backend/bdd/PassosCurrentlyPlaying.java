package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.biblioteca.AlterarProgressoJogatina;
import com.g4.game_tracker_backend.aplicacao.biblioteca.ListarJogandoAgora;
import com.g4.game_tracker_backend.aplicacao.biblioteca.ListarJogandoAgora.JogoEmAndamento;
import com.g4.game_tracker_backend.aplicacao.biblioteca.MarcarJogandoAgora;
import com.g4.game_tracker_backend.aplicacao.biblioteca.RemoverJogandoAgora;
import com.g4.game_tracker_backend.dominio.biblioteca.ProgressoJogatina;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/** Steps da F-07, sobre os casos de uso reais e o jogador autenticado de {@link DadosDoCenario}. */
public class PassosCurrentlyPlaying {

    private final MarcarJogandoAgora marcar;
    private final AlterarProgressoJogatina alterarProgresso;
    private final RemoverJogandoAgora remover;
    private final ListarJogandoAgora listar;
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;

    private Instant inicioDoCenario;

    public PassosCurrentlyPlaying(MarcarJogandoAgora marcar, AlterarProgressoJogatina alterarProgresso,
            RemoverJogandoAgora remover, ListarJogandoAgora listar, DadosDoCenario dados,
            ContextoDoCenario contexto) {
        this.marcar = marcar;
        this.alterarProgresso = alterarProgresso;
        this.remover = remover;
        this.listar = listar;
        this.dados = dados;
        this.contexto = contexto;
    }

    // ---- Dado ----

    @Dado("que o jogador tem o jogo {string} em Currently Playing com progresso {string}")
    public void jogadorTemJogoComProgresso(String jogo, String progresso) {
        marcar.executar(idDo(jogo), null);
        ProgressoJogatina desejado = ProgressoJogatina.valueOf(progresso);
        if (desejado != ProgressoJogatina.NO_INICIO) {
            alterarProgresso.executar(idDo(jogo), desejado);
        }
    }

    @Dado("que o jogador tem {int} jogos em Currently Playing")
    public void jogadorTemJogos(int quantidade) {
        for (int i = 1; i <= quantidade; i++) {
            marcar.executar(idDo("Jogo em andamento " + i), null);
        }
    }

    @Dado("que o jogador tem {int} jogos em Currently Playing, entre eles {string}")
    public void jogadorTemJogosEntreEles(int quantidade, String jogo) {
        marcar.executar(idDo(jogo), null);
        jogadorTemJogos(quantidade - 1);
    }

    // ---- Quando ----

    @Quando("o jogador marca o jogo {string} como Currently Playing")
    public void jogadorMarca(String jogo) {
        inicioDoCenario = Instant.now();
        contexto.executar(() -> marcar.executar(idDo(jogo), null));
    }

    @Quando("o jogador altera o progresso de {string} para {string}")
    public void jogadorAlteraProgresso(String jogo, String progresso) {
        contexto.executar(() -> alterarProgresso.executar(idDo(jogo), ProgressoJogatina.valueOf(progresso)));
    }

    @Quando("o jogador substitui {string} por {string}")
    public void jogadorSubstitui(String substituido, String novo) {
        contexto.executar(() -> marcar.executar(idDo(novo), idDo(substituido)));
    }

    @Quando("o jogador remove {string} do Currently Playing")
    public void jogadorRemove(String jogo) {
        contexto.executar(() -> remover.executar(idDo(jogo)));
    }

    // ---- Então ----

    @Entao("o jogo {string} deve aparecer no Currently Playing do jogador")
    public void jogoDeveAparecer(String jogo) {
        assertTrue(registroDo(jogo) != null, "O jogo " + jogo + " não está em andamento");
    }

    @Entao("o jogo {string} não deve aparecer no Currently Playing do jogador")
    public void jogoNaoDeveAparecer(String jogo) {
        assertEquals(null, registroDo(jogo));
    }

    /** RN 3: o instante guarda data e hora, não só o dia. */
    @Entao("o registro deve ter a data e a hora de início preenchidas")
    public void registroDeveTerDataEHora() {
        JogoEmAndamento registro = emAndamento().get(0);
        Instant iniciadoEm = registro.registro().iniciadoEm();
        assertNotNull(iniciadoEm);
        assertTrue(!iniciadoEm.isBefore(inicioDoCenario.minus(Duration.ofSeconds(1)))
                && !iniciadoEm.isAfter(Instant.now()),
                "O início deveria ser o instante da marcação, mas foi " + iniciadoEm);
    }

    @Entao("o progresso de {string} deve ser {string}")
    public void progressoDeveSer(String jogo, String progresso) {
        assertEquals(ProgressoJogatina.valueOf(progresso), registroDo(jogo).registro().progresso());
    }

    @Entao("o jogador deve continuar com {int} jogos em Currently Playing")
    public void jogadorDeveContinuarCom(int quantidade) {
        assertEquals(quantidade, emAndamento().size());
    }

    /** O "Quando" é o step da F-08 (PassosPerfilFavoritos), que autentica um visitante. */
    @Entao("o perfil deve exibir {int} jogos em Currently Playing")
    public void perfilDeveExibir(int quantidade) {
        assertEquals(quantidade, listar.executarPara(dados.jogadorAtual().id()).size());
    }

    private List<JogoEmAndamento> emAndamento() {
        return listar.executarPara(dados.jogadorAtual().id());
    }

    private JogoEmAndamento registroDo(String jogo) {
        UUID jogoId = idDo(jogo);
        return emAndamento().stream().filter(j -> j.registro().jogoId().equals(jogoId)).findFirst().orElse(null);
    }

    private UUID idDo(String jogo) {
        return dados.jogo(jogo).id();
    }
}
