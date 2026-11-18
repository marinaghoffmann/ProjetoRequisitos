package com.g4.game_tracker_backend.aplicacao.journal;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.journal.AnexoJournalRepositorio;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournalRepositorio;
import com.g4.game_tracker_backend.dominio.journal.ImagemDoJournal;

/** Casos de uso do diário manual. Entrada e imagens são gravadas ou desfeitas juntas. */
public class GerenciarJournal {
    public record DadosEntrada(UUID jogoId, LocalDate dataSessao, String titulo, String texto,
            List<String> tags, boolean contemSpoiler) { }

    private final EntradaDeJournalRepositorio entradas;
    private final AnexoJournalRepositorio anexos;
    private final JogoRepositorio jogos;
    private final VerificadorDeAcesso acesso;
    private final ExecutorDeTransacao transacao;
    private final Clock relogio;

    public GerenciarJournal(EntradaDeJournalRepositorio entradas, AnexoJournalRepositorio anexos,
            JogoRepositorio jogos, VerificadorDeAcesso acesso, ExecutorDeTransacao transacao, Clock relogio) {
        this.entradas = entradas;
        this.anexos = anexos;
        this.jogos = jogos;
        this.acesso = acesso;
        this.transacao = transacao;
        this.relogio = relogio;
    }

    public EntradaDeJournal criar(DadosEntrada dados, List<ImagemDoJournal> imagens) {
        return transacao.executar(() -> {
            UUID dono = acesso.usuarioAtualId();
            validarDados(dados, true);
            var entrada = EntradaDeJournal.criarManual(dono, dados.jogoId(),
                    dados.dataSessao().atStartOfDay(ZoneOffset.UTC).toInstant(), dados.titulo(), dados.texto(),
                    dados.tags(), dados.contemSpoiler(), relogio.instant());
            var completa = prepararImagens(entrada, imagens, dono);
            entradas.salvar(entrada);
            gravarImagens(entrada, completa, imagens);
            entradas.salvar(completa);
            return completa;
        });
    }

    public EntradaDeJournal editar(UUID id, DadosEntrada dados, List<UUID> manter, List<ImagemDoJournal> imagens) {
        return transacao.executar(() -> {
            UUID dono = acesso.usuarioAtualId();
            var anterior = buscarParaAlterar(id);
            anterior.exigirEdicaoManual(dono);
            validarDados(dados, dados != null && !Objects.equals(anterior.jogoId(), dados.jogoId()));
            var editada = anterior.editar(dono, dados.jogoId(),
                    dados.dataSessao().atStartOfDay(ZoneOffset.UTC).toInstant(), dados.titulo(), dados.texto(),
                    dados.tags(), dados.contemSpoiler(), relogio.instant());
            var mantida = editada.manterAnexos(dono, manter == null
                    ? anterior.anexos().stream().map(a -> a.id()).toList() : manter, relogio.instant());
            var completa = prepararImagens(mantida, imagens, dono);
            for (var anexo : anterior.anexos()) {
                if (mantida.anexos().stream().noneMatch(a -> a.id().equals(anexo.id()))) {
                    anexos.remover(id, anexo.id());
                }
            }
            gravarImagens(mantida, completa, imagens);
            entradas.salvar(completa);
            return completa;
        });
    }

    public EntradaDeJournal anexarImagem(UUID id, ImagemDoJournal imagem) {
        return transacao.executar(() -> {
            UUID dono = acesso.usuarioAtualId();
            var anterior = buscarParaAlterar(id);
            var completa = prepararImagens(anterior, List.of(imagem), dono);
            gravarImagens(anterior, completa, List.of(imagem));
            entradas.salvar(completa);
            return completa;
        });
    }

    public void excluir(UUID id) {
        transacao.executarSemRetorno(() -> {
            var entrada = buscarParaAlterar(id);
            entrada.exigirEdicaoManual(acesso.usuarioAtualId());
            entradas.remover(id);
        });
    }

    private EntradaDeJournal prepararImagens(EntradaDeJournal entrada, List<ImagemDoJournal> imagens, UUID dono) {
        var resultado = entrada;
        for (var imagem : imagens == null ? List.<ImagemDoJournal>of() : imagens) {
            resultado = resultado.anexar(dono, imagem.metadados(), relogio.instant());
        }
        return resultado;
    }

    private void gravarImagens(EntradaDeJournal anterior, EntradaDeJournal completa, List<ImagemDoJournal> imagens) {
        if (imagens == null) return;
        int inicio = anterior.anexos().size();
        for (int i = 0; i < imagens.size(); i++) {
            anexos.adicionar(completa.id(), completa.anexos().get(inicio + i), imagens.get(i).conteudo());
        }
    }

    private EntradaDeJournal buscarParaAlterar(UUID id) {
        return entradas.buscarParaAlterar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Entrada não encontrada"));
    }

    private void validarDados(DadosEntrada dados, boolean exigirAtivo) {
        if (dados == null || dados.dataSessao() == null) {
            throw new RegraDeNegocioException("A data da jogatina é obrigatória");
        }
        if (dados.jogoId() != null) {
            var jogo = jogos.buscarPorId(dados.jogoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
            if (exigirAtivo && jogo.status() != StatusCatalogo.ATIVO) {
                throw new RegraDeNegocioException("O jogo precisa estar ativo no catálogo");
            }
        }
    }
}
