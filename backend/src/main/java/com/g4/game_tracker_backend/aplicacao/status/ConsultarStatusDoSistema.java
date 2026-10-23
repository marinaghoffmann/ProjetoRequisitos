package com.g4.game_tracker_backend.aplicacao.status;

import java.time.Instant;

import org.springframework.stereotype.Service;

/**
 * Caso de uso mínimo que serve de exemplo do fluxo apresentação → aplicação e de alvo
 * para o cenário BDD de verificação da infraestrutura (f00_infraestrutura.feature).
 */
@Service
public class ConsultarStatusDoSistema {

    public StatusDoSistema executar() {
        return new StatusDoSistema("game-tracker", "UP", Instant.now());
    }

    public record StatusDoSistema(String aplicacao, String status, Instant verificadoEm) {
    }
}
