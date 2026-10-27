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

@Service
public class ImportarJogo {

    private final JogoRepositorio jogos;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final GeneroRepositorio generos;

    public ImportarJogo(JogoRepositorio jogos, DesenvolvedoraRepositorio desenvolvedoras,
            GeneroRepositorio generos) {
        this.jogos = jogos;
        this.desenvolvedoras = desenvolvedoras;
        this.generos = generos;
    }

    @Transactional
    public Jogo executar(String titulo, String tituloOriginal, String resumo, String identificadorExterno,
            UUID desenvolvedoraId, Set<UUID> generosIds, LocalDate dataLancamento, String capaUrl,
            String trailerUrl) {
        Desenvolvedora desenvolvedora = desenvolvedoras.buscarPorId(desenvolvedoraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desenvolvedora não encontrada"));
        validarGeneros(generosIds);

        Jogo existente = jogos.buscarPorIdentificadorExterno(identificadorExterno).orElse(null);
        if (existente != null) {
            return jogos.salvar(existente.atualizarDados(titulo, tituloOriginal, resumo,
                    desenvolvedora.id(), generosIds, dataLancamento, capaUrl, trailerUrl));
        }
        return jogos.salvar(Jogo.importar(titulo, tituloOriginal, resumo, identificadorExterno,
                desenvolvedora.id(), generosIds, dataLancamento, capaUrl, trailerUrl));
    }

    private void validarGeneros(Set<UUID> generosIds) {
        if (generosIds == null) {
            return;
        }
        for (UUID generoId : generosIds) {
            generos.buscarPorId(generoId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Gênero não encontrado: " + generoId));
        }
    }
}
