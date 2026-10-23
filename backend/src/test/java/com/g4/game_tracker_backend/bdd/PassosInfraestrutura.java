package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.g4.game_tracker_backend.aplicacao.status.ConsultarStatusDoSistema;
import com.g4.game_tracker_backend.aplicacao.status.ConsultarStatusDoSistema.StatusDoSistema;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/** Steps do cenário de exemplo f00_infraestrutura.feature. Use como modelo. */
public class PassosInfraestrutura {

    private final ConsultarStatusDoSistema consultarStatus;
    private final ContextoDoCenario contexto;
    private StatusDoSistema status;

    public PassosInfraestrutura(ConsultarStatusDoSistema consultarStatus, ContextoDoCenario contexto) {
        this.consultarStatus = consultarStatus;
        this.contexto = contexto;
    }

    @Quando("consulto o status do sistema")
    public void consultoOStatusDoSistema() {
        status = contexto.obter(consultarStatus::executar);
    }

    @Quando("uma regra de negócio é violada com a mensagem {string}")
    public void umaRegraDeNegocioEViolada(String mensagem) {
        contexto.executar(() -> {
            throw new RegraDeNegocioException(mensagem);
        });
    }

    @Entao("o status deve ser {string}")
    public void oStatusDeveSer(String esperado) {
        assertEquals(esperado, status.status());
    }
}
