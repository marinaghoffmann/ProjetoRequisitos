package com.g4.game_tracker_backend.apresentacao.avaliacao.estatisticas;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas.ConsultarEstatisticas;

@RestController
public class EstatisticasJogoController {

    private final ConsultarEstatisticas consultarEstatisticas;

    public EstatisticasJogoController(ConsultarEstatisticas consultarEstatisticas) {
        this.consultarEstatisticas = consultarEstatisticas;
    }

    @GetMapping("/api/catalogo/jogos/{jogoId}/estatisticas")
    public EstatisticaJogoResponse consultar(@PathVariable UUID jogoId) {
        return EstatisticaJogoResponse.de(consultarEstatisticas.consultarFicha(jogoId));
    }
}
