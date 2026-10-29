package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

/** F-01 RN 1 e RN 2: cadastro manual feito por um desenvolvedor vinculado a uma desenvolvedora ativa. */
@Service
public class CadastrarJogoManual {

    private final JogoRepositorio jogos;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final GeneroRepositorio generos;
    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public CadastrarJogoManual(JogoRepositorio jogos, DesenvolvedoraRepositorio desenvolvedoras,
            GeneroRepositorio generos, UsuarioRepositorio usuarios, VerificadorDeAcesso verificadorDeAcesso) {
        this.jogos = jogos;
        this.desenvolvedoras = desenvolvedoras;
        this.generos = generos;
        this.usuarios = usuarios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public Jogo executar(String titulo, String tituloOriginal, String resumo, Set<UUID> generosIds,
            LocalDate dataLancamento, String capaUrl, String trailerUrl, String metadadosJson) {
        if (!verificadorDeAcesso.usuarioAtualTemAlgumPapel(Papel.DESENVOLVEDOR)) {
            throw new AcessoNegadoException("Apenas desenvolvedores podem cadastrar jogos manualmente");
        }
        var usuario = usuarios.buscarPorId(verificadorDeAcesso.usuarioAtualId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado"));
        UUID desenvolvedoraId = usuario.desenvolvedoraId();
        boolean vinculadaAtiva = desenvolvedoraId != null && desenvolvedoras.buscarPorId(desenvolvedoraId)
                .map(desenvolvedora -> desenvolvedora.ativa())
                .orElse(false);
        if (!vinculadaAtiva) {
            throw new RegraDeNegocioException("O desenvolvedor precisa estar vinculado a uma desenvolvedora ativa");
        }
        if (jogos.existePorTituloEDesenvolvedora(titulo, desenvolvedoraId)) {
            throw new RegraDeNegocioException("Já existe um jogo com este título para a desenvolvedora");
        }
        ValidacaoCatalogo.exigirGeneros(generos, generosIds);
        return jogos.salvar(Jogo.criarManual(titulo, tituloOriginal, resumo, desenvolvedoraId, generosIds,
                dataLancamento, capaUrl, trailerUrl).comMetadados(metadadosJson));
    }
}
