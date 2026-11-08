package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;

/**
 * Steps reutilizados por todas as funcionalidades.
 *
 * O step de recusa cobre todas as variações já escritas nos .feature, como
 * "o cadastro deve ser recusado com a mensagem", "a operação deve ser recusada com a mensagem",
 * "o acesso deve ser recusado com a mensagem" etc. Não crie outro step com essa frase.
 */
public class PassosComuns {

    private final ContextoDoCenario contexto;
    private final DadosDoCenario dados;

    public PassosComuns(ContextoDoCenario contexto, DadosDoCenario dados) {
        this.contexto = contexto;
        this.dados = dados;
    }

    /** Jogador comum autenticado; os outros steps o acessam por DadosDoCenario.jogadorAtual(). */
    @Dado("o jogador {string} autenticado")
    public void jogadorAutenticado(String nome) {
        dados.autenticar(nome);
    }

    @Entao("^(?:o|a) \\S+ deve ser recusad[oa] com a mensagem \"([^\"]*)\"$")
    public void deveSerRecusadoComAMensagem(String mensagemEsperada) {
        assertNotNull(contexto.erro(),
                "Esperava que a ação fosse recusada com \"" + mensagemEsperada + "\", mas ela foi aceita");
        assertEquals(mensagemEsperada, contexto.erro().getMessage());
    }

    @Entao("a operação deve ser concluída sem erros")
    public void deveSerConcluidaSemErros() {
        assertNull(contexto.erro(), () -> "A ação foi recusada: " + contexto.erro().getMessage());
    }
}
