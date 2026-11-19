package com.g4.game_tracker_backend.aplicacao.status;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;

import java.time.Instant;

/**
 * Caso de uso mínimo que serve de exemplo do fluxo apresentação → aplicação e de alvo
 * para o cenário BDD de verificação da infraestrutura (f00_infraestrutura.feature).
 */
@CasoDeUso
public class ConsultarStatusDoSistema {

    public StatusDoSistema executar() {
        return new StatusDoSistema("game-tracker", "UP", Instant.now());
    }

    public record StatusDoSistema(String aplicacao, String status, Instant verificadoEm) {
    }
}
