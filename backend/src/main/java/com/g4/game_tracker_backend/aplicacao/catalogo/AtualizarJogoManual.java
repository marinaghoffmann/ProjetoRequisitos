package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

/**
 * Edição de metadados: o administrador edita qualquer jogo; o desenvolvedor, apenas os jogos da
 * desenvolvedora a que está vinculado, sem transferi-los para outra.
 */
@Service
public class AtualizarJogoManual {

    private final JogoRepositorio jogos;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final GeneroRepositorio generos;
    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public AtualizarJogoManual(JogoRepositorio jogos, DesenvolvedoraRepositorio desenvolvedoras,
            GeneroRepositorio generos, UsuarioRepositorio usuarios, VerificadorDeAcesso verificadorDeAcesso) {
        this.jogos = jogos;
        this.desenvolvedoras = desenvolvedoras;
        this.generos = generos;
        this.usuarios = usuarios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public Jogo executar(UUID id, String titulo, String tituloOriginal, String resumo, UUID desenvolvedoraId,
            Set<UUID> generosIds, LocalDate dataLancamento, String capaUrl, String trailerUrl, String metadadosJson) {
        Jogo atual = jogos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        UUID novaDesenvolvedoraId = desenvolvedoraId == null ? atual.desenvolvedoraId() : desenvolvedoraId;
        exigirPermissao(atual, novaDesenvolvedoraId);
        Desenvolvedora desenvolvedora = desenvolvedoras.buscarPorId(novaDesenvolvedoraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desenvolvedora não encontrada"));
        if (!desenvolvedora.ativa()) {
            throw new RegraDeNegocioException("A desenvolvedora precisa estar ativa");
        }
        if (jogos.existePorTituloEDesenvolvedoraEIdDiferente(titulo, novaDesenvolvedoraId, id)) {
            throw new RegraDeNegocioException("Já existe um jogo com este título para a desenvolvedora");
        }
        ValidacaoCatalogo.exigirGeneros(generos, generosIds);
        return jogos.salvar(atual.atualizarDados(titulo, tituloOriginal, resumo, novaDesenvolvedoraId,
                generosIds, dataLancamento, capaUrl, trailerUrl).comMetadados(metadadosJson));
    }

    private void exigirPermissao(Jogo atual, UUID novaDesenvolvedoraId) {
        if (verificadorDeAcesso.usuarioAtualTemAlgumPapel(Papel.ADMINISTRADOR)) {
            return;
        }
        verificadorDeAcesso.exigirPapel(Papel.DESENVOLVEDOR);
        UUID vinculada = usuarios.buscarPorId(verificadorDeAcesso.usuarioAtualId())
                .map(usuario -> usuario.desenvolvedoraId())
                .orElse(null);
        if (vinculada == null || !vinculada.equals(atual.desenvolvedoraId())
                || !vinculada.equals(novaDesenvolvedoraId)) {
            throw new AcessoNegadoException("O desenvolvedor só pode editar jogos da própria desenvolvedora");
        }
    }
}
