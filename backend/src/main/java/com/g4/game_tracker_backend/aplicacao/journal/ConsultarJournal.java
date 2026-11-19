package com.g4.game_tracker_backend.aplicacao.journal;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.dominio.journal.AnexoJournal;
import com.g4.game_tracker_backend.dominio.journal.AnexoJournalRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.ConteudoOriginal;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournalRepositorio;
import com.g4.game_tracker_backend.dominio.journal.FiltroJournal;
import com.g4.game_tracker_backend.dominio.compartilhado.IConteudoExibivel;
import com.g4.game_tracker_backend.dominio.journal.SpoilerMascaradoDecorator;
import com.g4.game_tracker_backend.dominio.journal.TipoOrigem;
import com.g4.game_tracker_backend.dominio.moderacao.ConteudoRemovidoDecorator;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

public class ConsultarJournal {
    public record Entrada(UUID id, UUID jogoId, String jogoTitulo, String jogoCapaUrl, Instant dataSessao,
            String titulo, String texto, List<String> tags, List<AnexoJournal> anexos, boolean contemSpoiler,
            boolean mascarada, TipoOrigem tipoOrigem, boolean editavel, boolean moderada) { }
    public record Imagem(String tipoMidia, byte[] conteudo) { }

    private final EntradaDeJournalRepositorio entradas;
    private final AnexoJournalRepositorio anexos;
    private final JogoRepositorio jogos;
    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso acesso;
    private final ExecutorDeTransacao transacao;
    private final EstadoPublico moderacao;

    public ConsultarJournal(EntradaDeJournalRepositorio entradas, AnexoJournalRepositorio anexos,
            JogoRepositorio jogos, UsuarioRepositorio usuarios, VerificadorDeAcesso acesso,
            ExecutorDeTransacao transacao, EstadoPublico moderacao) {
        this.entradas = entradas;
        this.anexos = anexos;
        this.jogos = jogos;
        this.usuarios = usuarios;
        this.acesso = acesso;
        this.transacao = transacao;
        this.moderacao = moderacao;
    }

    public Pagina<Entrada> minhas(FiltroJournal filtro, int pagina, int tamanho) {
        return transacao.consultar(() -> filtrar(acesso.usuarioAtualId(), filtro, pagina, tamanho, false));
    }

    public Entrada minha(UUID id) {
        return transacao.consultar(() -> {
            var entrada = buscar(id);
            entrada.exigirDono(acesso.usuarioAtualId());
            return exibir(entrada, true, true, new HashMap<>());
        });
    }

    public List<String> tags(UUID donoPublico) {
        return transacao.consultar(() -> {
            UUID dono = donoPublico == null ? acesso.usuarioAtualId() : donoPublico;
            if (donoPublico != null) exigirPerfil(dono);
            return entradas.listarTags(dono);
        });
    }

    public Pagina<Entrada> publicas(UUID donoId, FiltroJournal filtro, int pagina, int tamanho) {
        return transacao.consultar(() -> {
            exigirPerfil(donoId);
            return filtrar(donoId, filtro, pagina, tamanho, true);
        });
    }

    public Entrada publica(UUID donoId, UUID id, boolean revelarSpoiler) {
        return transacao.consultar(() -> {
            exigirPerfil(donoId);
            var entrada = buscar(id);
            if (!entrada.donoId().equals(donoId)) throw new RecursoNaoEncontradoException("Entrada não encontrada");
            // F-14: entrada oculta ou removida sai do perfil público.
            moderacao.exigirVisivel(TipoConteudo.JOURNAL, id);
            return exibir(entrada, revelarSpoiler, false, new HashMap<>());
        });
    }

    public Imagem imagem(UUID entradaId, UUID anexoId, boolean revelarSpoiler) {
        return transacao.consultar(() -> {
            var entrada = buscar(entradaId);
            exigirPerfil(entrada.donoId());
            if (!ativa(entrada)) {
                throw new RecursoNaoEncontradoException("Imagem não encontrada");
            }
            if (entrada.contemSpoiler() && !revelarSpoiler) {
                throw new AcessoNegadoException("Revele o spoiler antes de visualizar as imagens");
            }
            var anexo = entrada.anexos().stream().filter(a -> a.id().equals(anexoId)).findFirst()
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Imagem não encontrada"));
            var bytes = anexos.conteudo(entradaId, anexoId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Imagem não encontrada"));
            return new Imagem(anexo.tipoMidia(), bytes);
        });
    }

    private Pagina<Entrada> filtrar(UUID dono, FiltroJournal filtro, int pagina, int tamanho, boolean publico) {
        if (pagina < 1 || tamanho < 1 || tamanho > 50) {
            throw new RegraDeNegocioException("Informe uma página positiva e tamanho entre 1 e 50");
        }
        // Cache por consulta: várias entradas da página costumam ser do mesmo jogo.
        Map<UUID, Optional<Jogo>> jogosDaPagina = new HashMap<>();
        var resultado = publico ? entradas.filtrarPublicas(dono, filtro, pagina, tamanho)
                : entradas.filtrar(dono, filtro, pagina, tamanho);
        return resultado.map(entrada -> exibir(entrada, !publico, !publico, jogosDaPagina));
    }

    private Entrada exibir(EntradaDeJournal entrada, boolean revelar, boolean proprio,
            Map<UUID, Optional<Jogo>> jogosConsultados) {
        // F-14: só o dono chega aqui com a entrada moderada; a removida mostra a mensagem de sistema.
        boolean removida = moderacao.removido(TipoConteudo.JOURNAL, entrada.id());
        boolean ativa = !removida && !moderacao.oculto(TipoConteudo.JOURNAL, entrada.id());
        IConteudoExibivel conteudo = new ConteudoOriginal(entrada.texto());
        if (entrada.contemSpoiler()) conteudo = new SpoilerMascaradoDecorator(conteudo, revelar);
        conteudo = new ConteudoRemovidoDecorator(conteudo, removida);
        boolean mascarada = entrada.contemSpoiler() && !revelar && !removida;
        var jogo = entrada.jogoId() == null ? Optional.<Jogo>empty()
                : jogosConsultados.computeIfAbsent(entrada.jogoId(), jogos::buscarPorId);
        String titulo = entrada.titulo() == null
                ? (entrada.automatica() ? entrada.texto() : "Anotação de jogatina") : entrada.titulo();
        return new Entrada(entrada.id(), entrada.jogoId(), jogo.map(Jogo::titulo).orElse(null),
                jogo.map(Jogo::capaUrl).orElse(null), entrada.dataSessao(),
                new ConteudoRemovidoDecorator(new ConteudoOriginal(titulo), removida).exibir(),
                conteudo.exibir(), entrada.tags(), mascarada || !ativa ? List.of() : entrada.anexos(),
                entrada.contemSpoiler(), mascarada, entrada.tipoOrigem(), proprio && !entrada.automatica() && ativa,
                !ativa);
    }

    private boolean ativa(EntradaDeJournal entrada) {
        return !moderacao.oculto(TipoConteudo.JOURNAL, entrada.id())
                && !moderacao.removido(TipoConteudo.JOURNAL, entrada.id());
    }

    private EntradaDeJournal buscar(UUID id) {
        return entradas.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Entrada não encontrada"));
    }

    private void exigirPerfil(UUID dono) {
        usuarios.buscarPorId(dono).filter(Usuario::ativo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil não encontrado"));
    }
}
