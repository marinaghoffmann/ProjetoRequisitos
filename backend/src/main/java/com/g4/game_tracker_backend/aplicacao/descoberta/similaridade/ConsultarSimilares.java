package com.g4.game_tracker_backend.aplicacao.descoberta.similaridade;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.FeedbackDeSugestao;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.FeedbackSugestaoRepositorio;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeJogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeRepositorio;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.TipoFeedback;

/**
 * Consulta pública da F-10: os similares de um jogo, do maior para o menor grau de relevância, com limite.
 *
 * notaMinima filtra pela avaliação da comunidade: usa a média geral da ficha de estatísticas (F-17) do jogo
 * sugerido, e só jogos elegíveis (3 ou mais reviews) entram. Se o jogo ainda não tem similares calculados, o
 * cálculo roda aqui, sob demanda. Cada sugestão traz a contagem de feedbacks e o feedback do usuário logado.
 */
@Service
public class ConsultarSimilares {

    public static final int LIMITE_PADRAO = 10;
    public static final int LIMITE_MAXIMO = 50;

    /** Uma sugestão vista a partir do jogo consultado: o outro jogo do par e o feedback de quem consulta. */
    public record Similar(SimilaridadeJogo similaridade, UUID jogoId, String titulo, String capaUrl,
            TipoFeedback meuFeedback) {
    }

    private final SimilaridadeRepositorio similaridades;
    private final FeedbackSugestaoRepositorio feedbacks;
    private final RecalcularSimilaridades recalcular;
    private final JogoRepositorio jogos;
    private final EstatisticaJogoRepositorio estatisticas;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public ConsultarSimilares(SimilaridadeRepositorio similaridades, FeedbackSugestaoRepositorio feedbacks,
            RecalcularSimilaridades recalcular, JogoRepositorio jogos, EstatisticaJogoRepositorio estatisticas,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.similaridades = similaridades;
        this.feedbacks = feedbacks;
        this.recalcular = recalcular;
        this.jogos = jogos;
        this.estatisticas = estatisticas;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public List<Similar> consultar(UUID jogoId, BigDecimal notaMinima, Integer limite) {
        int quantidade = limite == null ? LIMITE_PADRAO : limite;
        if (quantidade < 1 || quantidade > LIMITE_MAXIMO) {
            throw new RegraDeNegocioException("O limite deve estar entre 1 e " + LIMITE_MAXIMO);
        }
        if (notaMinima != null && (notaMinima.signum() < 0 || notaMinima.compareTo(BigDecimal.valueOf(5)) > 0)) {
            throw new RegraDeNegocioException("A nota mínima deve estar entre 0 e 5");
        }
        Jogo jogo = jogos.buscarPorId(jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        if (jogo.status() == StatusCatalogo.ATIVO && !similaridades.existeParaJogo(jogoId)) {
            recalcular.recalcular(jogoId);
        }

        Optional<UUID> usuario = Sugestoes.usuarioAtualSeHouver(verificadorDeAcesso);
        List<SimilaridadeJogo> visiveis = similaridades.listarDoJogo(jogoId).stream()
                .filter(SimilaridadeJogo::visivel)
                .toList();
        Map<UUID, TipoFeedback> meus = usuario
                .map(id -> feedbacks.listarDoUsuario(id, visiveis.stream().map(SimilaridadeJogo::id).toList()))
                .orElse(List.of()).stream()
                .collect(Collectors.toMap(FeedbackDeSugestao::similaridadeId, FeedbackDeSugestao::tipo));

        return visiveis.stream()
                .map(similaridade -> similar(jogoId, similaridade, meus))
                .flatMap(Optional::stream)
                .filter(similar -> atendeNotaMinima(similar.jogoId(), notaMinima))
                .sorted(Comparator.comparing((Similar similar) -> similar.similaridade().grauRelevancia())
                        .reversed()
                        .thenComparing(Similar::titulo))
                .limit(quantidade)
                .toList();
    }

    /** O outro jogo do par precisa continuar ATIVO no catálogo para ser sugerido. */
    private Optional<Similar> similar(UUID jogoId, SimilaridadeJogo similaridade, Map<UUID, TipoFeedback> meus) {
        UUID outro = similaridade.outroJogo(jogoId);
        return jogos.buscarPorId(outro)
                .filter(jogo -> jogo.status() == StatusCatalogo.ATIVO)
                .map(jogo -> new Similar(similaridade, outro, jogo.titulo(), jogo.capaUrl(),
                        meus.get(similaridade.id())));
    }

    private boolean atendeNotaMinima(UUID jogoId, BigDecimal notaMinima) {
        if (notaMinima == null) {
            return true;
        }
        return estatisticas.buscarPorJogo(jogoId)
                .filter(EstatisticaJogo::elegivelParaExibicao)
                .map(EstatisticaJogo::mediaGeral)
                .filter(media -> media.compareTo(notaMinima) >= 0)
                .isPresent();
    }
}
