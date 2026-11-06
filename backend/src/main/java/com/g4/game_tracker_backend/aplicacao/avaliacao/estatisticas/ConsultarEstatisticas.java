package com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;

/** Consulta pública da F-17: a ficha de estatísticas consolidadas de um jogo. */
@Service
public class ConsultarEstatisticas {

    private final EstatisticaJogoRepositorio estatisticas;
    private final JogoRepositorio jogos;

    public ConsultarEstatisticas(EstatisticaJogoRepositorio estatisticas, JogoRepositorio jogos) {
        this.estatisticas = estatisticas;
        this.jogos = jogos;
    }

    @Transactional(readOnly = true)
    public EstatisticaJogo consultarFicha(UUID jogoId) {
        if (jogos.buscarPorId(jogoId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Jogo não encontrado");
        }
        return estatisticas.buscarPorJogo(jogoId).orElseGet(() -> EstatisticaJogo.semDados(jogoId));
    }
}
