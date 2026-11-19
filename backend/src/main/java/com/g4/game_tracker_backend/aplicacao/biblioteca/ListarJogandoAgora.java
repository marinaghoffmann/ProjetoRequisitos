package com.g4.game_tracker_backend.aplicacao.biblioteca;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.biblioteca.JogandoAgora;
import com.g4.game_tracker_backend.dominio.biblioteca.JogandoAgoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

@CasoDeUso
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

    /** Jogos em andamento do usuário autenticado. */
    @Transacional(somenteLeitura = true)
    public List<JogoEmAndamento> executar() {
        return executarPara(acesso.usuarioAtualId());
    }

    /** Jogos em andamento de qualquer usuário, para o perfil público. Só registros ativos. */
    @Transacional(somenteLeitura = true)
    public List<JogoEmAndamento> executarPara(UUID usuarioId) {
        return registros.buscarAtivos(usuarioId).stream()
                .map(r -> jogos.buscarPorId(r.jogoId())
                        .map(j -> new JogoEmAndamento(r, j.titulo(), j.capaUrl()))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }
}