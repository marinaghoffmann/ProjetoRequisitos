package com.g4.game_tracker_backend.aplicacao.biblioteca;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.biblioteca.acervo.GarantirNaBiblioteca;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.biblioteca.JogandoAgora;
import com.g4.game_tracker_backend.dominio.biblioteca.JogandoAgoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

@CasoDeUso
public class MarcarJogandoAgora {

    private final JogandoAgoraRepositorio registros;
    private final JogoRepositorio jogos;
    private final VerificadorDeAcesso acesso;
    private final GarantirNaBiblioteca garantirNaBiblioteca;

    public MarcarJogandoAgora(JogandoAgoraRepositorio registros, JogoRepositorio jogos, VerificadorDeAcesso acesso,
            GarantirNaBiblioteca garantirNaBiblioteca) {
        this.registros = registros;
        this.jogos = jogos;
        this.acesso = acesso;
        this.garantirNaBiblioteca = garantirNaBiblioteca;
    }

    @Transacional
    public void executar(UUID jogoId, UUID substituirJogoId) {
        UUID usuarioId = acesso.usuarioAtualId();
        if (jogos.buscarPorId(jogoId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Jogo não encontrado");
        }
        if (registros.buscarAtivo(usuarioId, jogoId).isPresent()) {
            throw new RegraDeNegocioException("Você já está jogando este jogo");
        }
        // Jogar agora implica ter o jogo na biblioteca.
        garantirNaBiblioteca.executar(usuarioId, jogoId);
        // RN 1 e RN 2: no limite, exige escolher qual jogo substituir antes de gravar.
        if (registros.contarAtivos(usuarioId) >= JogandoAgora.LIMITE) {
            if (substituirJogoId == null) {
                throw new RegraDeNegocioException("Remova ou substitua um jogo para continuar");
            }
            JogandoAgora substituido = registros.buscarAtivo(usuarioId, substituirJogoId).orElseThrow(
                    () -> new RegraDeNegocioException("O jogo a substituir não está entre os seus jogos em andamento"));
            registros.salvar(substituido.encerrar());
        }
        registros.salvar(JogandoAgora.iniciar(usuarioId, jogoId, Instant.now()));
    }
}