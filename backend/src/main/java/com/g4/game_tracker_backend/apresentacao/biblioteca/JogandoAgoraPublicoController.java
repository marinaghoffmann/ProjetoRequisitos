package com.g4.game_tracker_backend.apresentacao.biblioteca;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.apresentacao.biblioteca.JogandoAgoraController.JogandoAgoraResponse;
import com.g4.game_tracker_backend.aplicacao.biblioteca.ListarJogandoAgora;

@RestController
@RequestMapping("/api/perfis/{usuarioId}/jogando-agora")
public class JogandoAgoraPublicoController {

    private final ListarJogandoAgora listar;

    public JogandoAgoraPublicoController(ListarJogandoAgora listar) {
        this.listar = listar;
    }

    @GetMapping
    public List<JogandoAgoraResponse> listar(@PathVariable UUID usuarioId) {
        return listar.executarPara(usuarioId).stream().map(JogandoAgoraResponse::de).toList();
    }
}