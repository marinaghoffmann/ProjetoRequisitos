package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.catalogo.FiltroCatalogo;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

@CasoDeUso
public class ConsultarCatalogo {

    private final JogoRepositorio jogos;

    public ConsultarCatalogo(JogoRepositorio jogos) {
        this.jogos = jogos;
    }

    @Transacional(somenteLeitura = true)
    public List<Jogo> executar(FiltroCatalogo filtro) {
        return jogos.consultar(filtro == null ? FiltroCatalogo.ativos() : filtro);
    }

    @Transacional(somenteLeitura = true)
    public Jogo buscarPorId(UUID id) {
        return jogos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
    }
}
