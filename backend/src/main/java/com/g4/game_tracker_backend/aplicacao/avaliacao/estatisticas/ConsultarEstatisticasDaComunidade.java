package com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.FiltroCatalogo;
import com.g4.game_tracker_backend.dominio.catalogo.Genero;
import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;

/**
 * F-17: as fichas de todos os jogos ativos do catálogo, para a tela "Estatísticas da comunidade".
 *
 * Um jogo que ainda não gerou nenhum evento não tem ficha gravada e aparece com a ficha vazia (oculta). A
 * RN 3 continua valendo: quem monta a resposta só mostra os indicadores das fichas elegíveis (3 reviews).
 */
@CasoDeUso
public class ConsultarEstatisticasDaComunidade {

    public enum Visibilidade {
        TODOS, PUBLICAS, OCULTAS
    }

    /** Um jogo com a sua ficha e os nomes dos gêneros. */
    public record Linha(Jogo jogo, List<String> generos, EstatisticaJogo ficha) {
    }

    /** Públicas primeiro, pela maior média; depois as ocultas, pelo maior número de reviews; empate pelo título. */
    private static final Comparator<Linha> ORDEM = Comparator
            .comparing((Linha l) -> !l.ficha().elegivelParaExibicao())
            .thenComparing(l -> l.ficha().elegivelParaExibicao() ? l.ficha().mediaGeral() : BigDecimal.ZERO,
                    Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(l -> l.ficha().totalReviewsAtivas(), Comparator.reverseOrder())
            .thenComparing(l -> l.jogo().titulo(), String.CASE_INSENSITIVE_ORDER);

    private final EstatisticaJogoRepositorio estatisticas;
    private final JogoRepositorio jogos;
    private final GeneroRepositorio generos;

    public ConsultarEstatisticasDaComunidade(EstatisticaJogoRepositorio estatisticas, JogoRepositorio jogos,
            GeneroRepositorio generos) {
        this.estatisticas = estatisticas;
        this.jogos = jogos;
        this.generos = generos;
    }

    @Transacional(somenteLeitura = true)
    public List<Linha> executar(Visibilidade visibilidade) {
        Map<UUID, EstatisticaJogo> fichas = estatisticas.listarTodas().stream()
                .collect(Collectors.toMap(EstatisticaJogo::jogoId, Function.identity()));
        Map<UUID, String> nomesDosGeneros = generos.listar().stream()
                .collect(Collectors.toMap(Genero::id, Genero::nome));
        Visibilidade filtro = visibilidade == null ? Visibilidade.TODOS : visibilidade;
        return jogos.consultar(FiltroCatalogo.ativos()).stream()
                .map(jogo -> new Linha(jogo,
                        jogo.generosIds().stream().map(nomesDosGeneros::get).filter(n -> n != null).sorted().toList(),
                        fichas.getOrDefault(jogo.id(), EstatisticaJogo.semDados(jogo.id()))))
                .filter(linha -> switch (filtro) {
                    case TODOS -> true;
                    case PUBLICAS -> linha.ficha().elegivelParaExibicao();
                    case OCULTAS -> !linha.ficha().elegivelParaExibicao();
                })
                .sorted(ORDEM)
                .toList();
    }
}
