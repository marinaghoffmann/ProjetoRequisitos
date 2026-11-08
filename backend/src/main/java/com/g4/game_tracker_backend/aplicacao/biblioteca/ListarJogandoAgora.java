package com.g4.game_tracker_backend.aplicacao.biblioteca;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.biblioteca.JogandoAgora;
import com.g4.game_tracker_backend.dominio.biblioteca.JogandoAgoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;

@Service
public class ListarJogandoAgora {

    public record JogoEmAndamento(JogandoAgora registro, String titulo, String capaUrl) {
    }

    private final JogandoAgoraRepositorio registros;
    private final JogoRepositorio jogos;
    private final VerificadorDeAcesso acesso;

    public ListarJogandoAgora(JogandoAgoraRepositorio registros, JogoRepositorio jogos, VerificadorDeAcesso acesso) {
        this.registros = registros;
        this.jogos = jogos;
        this.acesso = acesso;
    }

    @Transactional(readOnly = true)
    public List<JogoEmAndamento> executar() {
        return registros.buscarAtivos(acesso.usuarioAtualId()).stream()
                .map(r -> jogos.buscarPorId(r.jogoId())
                        .map(j -> new JogoEmAndamento(r, j.titulo(), j.capaUrl()))
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}