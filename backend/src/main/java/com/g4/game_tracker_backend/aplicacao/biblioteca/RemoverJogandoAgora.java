package com.g4.game_tracker_backend.aplicacao.biblioteca;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.biblioteca.JogandoAgoraRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;

@Service
public class RemoverJogandoAgora {

    private final JogandoAgoraRepositorio registros;
    private final VerificadorDeAcesso acesso;

    public RemoverJogandoAgora(JogandoAgoraRepositorio registros, VerificadorDeAcesso acesso) {
        this.registros = registros;
        this.acesso = acesso;
    }

    @Transactional
    public void executar(UUID jogoId) {
        var registro = registros.buscarAtivo(acesso.usuarioAtualId(), jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Você não está jogando este jogo"));
        registros.salvar(registro.encerrar());
    }
}