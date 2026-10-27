package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;

@Service
public class DesativarJogo {

    private final JogoRepositorio jogos;

    public DesativarJogo(JogoRepositorio jogos) {
        this.jogos = jogos;
    }

    @Transactional
    public Jogo executar(UUID id) {
        Jogo jogo = jogos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        return jogos.salvar(jogo.desativar());
    }
}
