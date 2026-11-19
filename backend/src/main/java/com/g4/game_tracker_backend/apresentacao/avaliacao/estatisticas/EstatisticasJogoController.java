package com.g4.game_tracker_backend.apresentacao.avaliacao.estatisticas;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas.ConsultarEstatisticas;
import com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas.ConsultarEstatisticasDaComunidade;
import com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas.ConsultarEstatisticasDaComunidade.Visibilidade;

@RestController
public class EstatisticasJogoController {

    private final ConsultarEstatisticas consultarEstatisticas;
    private final ConsultarEstatisticasDaComunidade consultarDaComunidade;

    public EstatisticasJogoController(ConsultarEstatisticas consultarEstatisticas,
            ConsultarEstatisticasDaComunidade consultarDaComunidade) {
        this.consultarEstatisticas = consultarEstatisticas;
        this.consultarDaComunidade = consultarDaComunidade;
    }

    /** Fichas de todos os jogos ativos; visibilidade = TODOS, PUBLICAS ou OCULTAS (F-17 RN 3). */
    @GetMapping("/api/estatisticas")
    public List<EstatisticaComunidadeResponse> listar(
            @RequestParam(defaultValue = "TODOS") Visibilidade visibilidade) {
        return consultarDaComunidade.executar(visibilidade).stream().map(EstatisticaComunidadeResponse::de).toList();
    }

    @GetMapping("/api/catalogo/jogos/{jogoId}/estatisticas")
    public EstatisticaJogoResponse consultar(@PathVariable UUID jogoId) {
        return EstatisticaJogoResponse.de(consultarEstatisticas.consultarFicha(jogoId));
    }
}
