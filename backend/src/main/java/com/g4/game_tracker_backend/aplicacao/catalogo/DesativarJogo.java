package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;

/** F-01 RN 3: a remoção feita pelo administrador é lógica, preservando os vínculos do jogo. */
@Service
public class DesativarJogo {

    private final JogoRepositorio jogos;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public DesativarJogo(JogoRepositorio jogos, VerificadorDeAcesso verificadorDeAcesso) {
        this.jogos = jogos;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public Jogo executar(UUID id) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
        Jogo jogo = jogos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        return jogos.salvar(jogo.desativar());
    }
}
