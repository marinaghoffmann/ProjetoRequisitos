package com.g4.game_tracker_backend.aplicacao.biblioteca;

import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.biblioteca.JogandoAgoraRepositorio;
import com.g4.game_tracker_backend.dominio.biblioteca.ProgressoJogatina;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

@CasoDeUso
public class AlterarProgressoJogatina {

    private final JogandoAgoraRepositorio registros;
    private final VerificadorDeAcesso acesso;

    public AlterarProgressoJogatina(JogandoAgoraRepositorio registros, VerificadorDeAcesso acesso) {
        this.registros = registros;
        this.acesso = acesso;
    }

    @Transacional
    public void executar(UUID jogoId, ProgressoJogatina progresso) {
        var registro = registros.buscarAtivo(acesso.usuarioAtualId(), jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Você não está jogando este jogo"));
        registros.salvar(registro.comProgresso(progresso));
    }
}