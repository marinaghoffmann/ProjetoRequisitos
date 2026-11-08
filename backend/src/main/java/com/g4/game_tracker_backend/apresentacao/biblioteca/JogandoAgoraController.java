package com.g4.game_tracker_backend.apresentacao.biblioteca;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.biblioteca.AlterarProgressoJogatina;
import com.g4.game_tracker_backend.aplicacao.biblioteca.ListarJogandoAgora;
import com.g4.game_tracker_backend.aplicacao.biblioteca.MarcarJogandoAgora;
import com.g4.game_tracker_backend.aplicacao.biblioteca.RemoverJogandoAgora;
import com.g4.game_tracker_backend.dominio.biblioteca.ProgressoJogatina;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/biblioteca/jogando-agora")
public class JogandoAgoraController {

    public record MarcarRequest(@NotNull UUID jogoId, UUID substituirJogoId) {
    }

    public record ProgressoRequest(@NotNull ProgressoJogatina progresso) {
    }

    public record JogandoAgoraResponse(UUID jogoId, String titulo, String capaUrl,
            ProgressoJogatina progresso, LocalDate dataInicio) {
    }

    private final MarcarJogandoAgora marcar;
    private final AlterarProgressoJogatina alterar;
    private final RemoverJogandoAgora remover;
    private final ListarJogandoAgora listar;

    public JogandoAgoraController(MarcarJogandoAgora marcar, AlterarProgressoJogatina alterar,
            RemoverJogandoAgora remover, ListarJogandoAgora listar) {
        this.marcar = marcar;
        this.alterar = alterar;
        this.remover = remover;
        this.listar = listar;
    }

    @GetMapping
    public List<JogandoAgoraResponse> listar() {
        return listar.executar().stream()
                .map(i -> new JogandoAgoraResponse(i.registro().jogoId(), i.titulo(), i.capaUrl(),
                        i.registro().progresso(), i.registro().dataInicio()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void marcar(@Valid @RequestBody MarcarRequest r) {
        marcar.executar(r.jogoId(), r.substituirJogoId());
    }

    @PatchMapping("/{jogoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarProgresso(@PathVariable UUID jogoId, @Valid @RequestBody ProgressoRequest r) {
        alterar.executar(jogoId, r.progresso());
    }

    @DeleteMapping("/{jogoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID jogoId) {
        remover.executar(jogoId);
    }
}