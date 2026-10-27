package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

@Service
public class AtualizarJogoManual {

    private final JogoRepositorio jogos;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final GeneroRepositorio generos;

    public AtualizarJogoManual(JogoRepositorio jogos, DesenvolvedoraRepositorio desenvolvedoras,
            GeneroRepositorio generos) {
        this.jogos = jogos;
        this.desenvolvedoras = desenvolvedoras;
        this.generos = generos;
    }

    @Transactional
    public Jogo executar(UUID id, String titulo, String tituloOriginal, String resumo, UUID desenvolvedoraId,
            Set<UUID> generosIds, LocalDate dataLancamento, String capaUrl, String trailerUrl, String metadadosJson) {
        Jogo atual = jogos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        Desenvolvedora desenvolvedora = desenvolvedoras.buscarPorId(desenvolvedoraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desenvolvedora não encontrada"));
        if (!desenvolvedora.ativa()) {
            throw new RegraDeNegocioException("A desenvolvedora precisa estar ativa");
        }
        if (jogos.existePorTituloEDesenvolvedoraEIdDiferente(titulo, desenvolvedoraId, id)) {
            throw new RegraDeNegocioException("Já existe um jogo com este título para a desenvolvedora");
        }
        validarGeneros(generosIds);
        return jogos.salvar(atual.atualizarDados(titulo, tituloOriginal, resumo, desenvolvedoraId,
                generosIds, dataLancamento, capaUrl, trailerUrl).comMetadados(metadadosJson));
    }

    private void validarGeneros(Set<UUID> generosIds) {
        if (generosIds == null) return;
        for (UUID generoId : generosIds) {
            generos.buscarPorId(generoId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Gênero não encontrado: " + generoId));
        }
    }
}
