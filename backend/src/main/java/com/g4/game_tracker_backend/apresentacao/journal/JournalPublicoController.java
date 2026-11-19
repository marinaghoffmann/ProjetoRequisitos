package com.g4.game_tracker_backend.apresentacao.journal;

import java.time.LocalDate;
import java.util.UUID;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.journal.ConsultarJournal;
import com.g4.game_tracker_backend.apresentacao.compartilhado.PaginaResponse;
import com.g4.game_tracker_backend.dominio.journal.FiltroJournal;
import com.g4.game_tracker_backend.dominio.journal.TipoOrigem;

@RestController
@RequestMapping("/api/perfis/{donoId}/journal")
public class JournalPublicoController {
    private final ConsultarJournal consultar;
    public JournalPublicoController(ConsultarJournal consultar) { this.consultar = consultar; }

    @GetMapping("/tags")
    public List<String> tags(@PathVariable UUID donoId) { return consultar.tags(donoId); }

    @GetMapping
    public PaginaResponse<ConsultarJournal.Entrada> listar(@PathVariable UUID donoId,
            @RequestParam(required = false) UUID jogoId, @RequestParam(required = false) String tag,
            @RequestParam(required = false) LocalDate inicio, @RequestParam(required = false) LocalDate fim,
            @RequestParam(required = false) TipoOrigem origem, @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.de(consultar.publicas(donoId, new FiltroJournal(jogoId, tag, inicio, fim, origem), pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ConsultarJournal.Entrada obter(@PathVariable UUID donoId, @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean revelarSpoiler) {
        return consultar.publica(donoId, id, revelarSpoiler);
    }
}
