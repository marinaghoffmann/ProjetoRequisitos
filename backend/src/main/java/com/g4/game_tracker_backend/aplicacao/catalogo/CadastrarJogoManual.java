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
import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

@Service
public class CadastrarJogoManual {

    private final JogoRepositorio jogos;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final GeneroRepositorio generos;
    private final IdentidadeAutenticada identidade;
    private final UsuarioRepositorio usuarios;

    public CadastrarJogoManual(JogoRepositorio jogos, DesenvolvedoraRepositorio desenvolvedoras,
            GeneroRepositorio generos, IdentidadeAutenticada identidade, UsuarioRepositorio usuarios) {
        this.jogos = jogos;
        this.desenvolvedoras = desenvolvedoras;
        this.generos = generos;
        this.identidade = identidade;
        this.usuarios = usuarios;
    }

    @Transactional
    public Jogo executarParaUsuario(String titulo, String tituloOriginal, String resumo,
            Set<UUID> generosIds, LocalDate dataLancamento, String capaUrl, String trailerUrl,
            String metadadosJson) {
        if (identidade == null || identidade.papel() != Papel.DESENVOLVEDOR) {
            throw new RegraDeNegocioException("Apenas desenvolvedores podem cadastrar jogos manualmente");
        }
        var usuario = usuarios.buscarPorId(identidade.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado"));
        if (usuario.desenvolvedoraId() == null) {
            throw new RegraDeNegocioException("O usuário desenvolvedor não possui uma desenvolvedora vinculada");
        }
        return executar(titulo, tituloOriginal, resumo, usuario.desenvolvedoraId(), generosIds,
                dataLancamento, capaUrl, trailerUrl, metadadosJson);
    }

    @Transactional
    public Jogo executar(String titulo, String tituloOriginal, String resumo, UUID desenvolvedoraId,
            Set<UUID> generosIds, LocalDate dataLancamento, String capaUrl, String trailerUrl) {
        return executar(titulo, tituloOriginal, resumo, desenvolvedoraId, generosIds, dataLancamento, capaUrl,
                trailerUrl, "");
    }

    @Transactional
    public Jogo executar(String titulo, String tituloOriginal, String resumo, UUID desenvolvedoraId,
            Set<UUID> generosIds, LocalDate dataLancamento, String capaUrl, String trailerUrl, String metadadosJson) {
        Desenvolvedora desenvolvedora = desenvolvedoras.buscarPorId(desenvolvedoraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desenvolvedora não encontrada"));
        if (!desenvolvedora.ativa()) {
            throw new RegraDeNegocioException("A desenvolvedora precisa estar ativa");
        }
        if (jogos.existePorTituloEDesenvolvedora(titulo, desenvolvedoraId)) {
            throw new RegraDeNegocioException("Já existe um jogo com este título para a desenvolvedora");
        }
        validarGeneros(generosIds);
        return jogos.salvar(Jogo.criarManual(titulo, tituloOriginal, resumo, desenvolvedoraId, generosIds,
                dataLancamento, capaUrl, trailerUrl).comMetadados(metadadosJson));
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
