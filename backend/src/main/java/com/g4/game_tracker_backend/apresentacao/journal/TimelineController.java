package com.g4.game_tracker_backend.apresentacao.journal;

import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.journal.ConsultarTimeline;
import com.g4.game_tracker_backend.apresentacao.compartilhado.PaginaResponse;
import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;

/** F-16: timeline unificada do jogador autenticado. O CRUD das entradas manuais é da F-15. */
@RestController
@RequestMapping("/api/journal")
public class TimelineController {

    private final ConsultarTimeline consultarTimeline;

    public TimelineController(ConsultarTimeline consultarTimeline) {
        this.consultarTimeline = consultarTimeline;
    }

    @GetMapping("/timeline")
    public PaginaResponse<EntradaTimelineResponse> timeline(@RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        Pagina<EntradaDeJournal> resultado = consultarTimeline.consultar(pagina, tamanho);
        Map<UUID, String> titulos = consultarTimeline.titulosDosJogos(
                resultado.itens().stream().map(EntradaDeJournal::jogoId).toList());
        return PaginaResponse.de(resultado.map(entrada -> EntradaTimelineResponse.de(entrada, titulos)));
    }
}
