package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.lancamentos.AlertaLancamentoRepositorio;
import com.g4.game_tracker_backend.dominio.lancamentos.InteresseLancamentoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;

/** F-01 RN 3: a remoção feita pelo administrador é lógica, preservando os vínculos do jogo. */
@Service
public class DesativarJogo {

    private final JogoRepositorio jogos;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final InteresseLancamentoRepositorio interesses;
    private final AlertaLancamentoRepositorio alertas;

    public DesativarJogo(JogoRepositorio jogos, VerificadorDeAcesso verificadorDeAcesso,
            InteresseLancamentoRepositorio interesses, AlertaLancamentoRepositorio alertas) {
        this.jogos = jogos;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.interesses = interesses;
        this.alertas = alertas;
    }

    @Transactional
    public Jogo executar(UUID id) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
        Jogo jogo = jogos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        Jogo desativado = jogos.salvar(jogo.desativar());
        for (var interesse : interesses.ativosPorJogo(id)) alertas.cancelarPendentes(interesse.id());
        return desativado;
    }
}
