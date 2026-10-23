package com.g4.game_tracker_backend.bdd;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

import io.cucumber.spring.ScenarioScope;

/**
 * Estado compartilhado entre as classes de steps dentro de UM cenário (é recriado a cada
 * cenário). Use executar(...) ou obter(...) nos steps "Quando" para que uma regra violada seja guardada
 * em vez de derrubar o teste; o step comum "deve ser recusado com a mensagem" confere depois.
 */
@Component
@ScenarioScope
public class ContextoDoCenario {

    private RuntimeException erro;

    /** Para ações sem retorno. */
    public void executar(Runnable acao) {
        obter(() -> {
            acao.run();
            return null;
        });
    }

    /** Para ações que devolvem um resultado; devolve null se a ação for recusada. */
    public <T> T obter(Supplier<T> acao) {
        try {
            return acao.get();
        } catch (RegraDeNegocioException | AcessoNegadoException | RecursoNaoEncontradoException e) {
            this.erro = e;
            return null;
        }
    }

    public RuntimeException erro() {
        return erro;
    }
}
