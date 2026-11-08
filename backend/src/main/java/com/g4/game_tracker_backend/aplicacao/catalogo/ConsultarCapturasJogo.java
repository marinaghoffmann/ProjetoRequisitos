package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;

@Service
public class ConsultarCapturasJogo {

    private final JogoRepositorio jogos;
    private final BuscadorDeCapturas buscador;

    public ConsultarCapturasJogo(JogoRepositorio jogos, BuscadorDeCapturas buscador) {
        this.jogos = jogos;
        this.buscador = buscador;
    }

    public List<BuscadorDeCapturas.Captura> executar(UUID jogoId) {
        Jogo jogo = jogos.buscarPorId(jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        String externo = jogo.identificadorExterno();
        if (externo == null || !externo.startsWith("STEAM-"))
            return List.of();
        String appId = externo.substring("STEAM-".length());
        return appId.isBlank() ? List.of() : buscador.buscar(appId);
    }
}