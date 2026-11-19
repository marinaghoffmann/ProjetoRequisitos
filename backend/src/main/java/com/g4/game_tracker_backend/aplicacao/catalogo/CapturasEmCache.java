package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Padrão <b>Proxy</b> (de cache), F-01: tem a mesma interface do {@link BuscadorDeCapturas} real (a Steam) e o
 * envolve. Abrir a página de um jogo deixa de repetir a requisição externa para os mesmos dados.
 * <ul>
 * <li>Guarda as capturas por {@code appId} durante a validade e só chama o real sem entrada ou com ela vencida.</li>
 * <li>Se o real falhar e houver entrada vencida, devolve a vencida em vez de quebrar a página; sem entrada
 * nenhuma, devolve lista vazia, como a página já fazia sem a Steam.</li>
 * <li>O cache é limitado: passando do máximo, descarta a entrada gravada há mais tempo.</li>
 * </ul>
 */
public class CapturasEmCache implements BuscadorDeCapturas {

    public static final int TAMANHO_MAXIMO_PADRAO = 500;

    private record Entrada(List<Captura> capturas, Instant obtidaEm) {
    }

    private final BuscadorDeCapturas real;
    private final Clock relogio;
    private final Duration validade;
    private final Map<String, Entrada> entradas;

    public CapturasEmCache(BuscadorDeCapturas real, Clock relogio, Duration validade) {
        this(real, relogio, validade, TAMANHO_MAXIMO_PADRAO);
    }

    public CapturasEmCache(BuscadorDeCapturas real, Clock relogio, Duration validade, int tamanhoMaximo) {
        this.real = Objects.requireNonNull(real);
        this.relogio = Objects.requireNonNull(relogio);
        this.validade = Objects.requireNonNull(validade);
        if (tamanhoMaximo < 1) {
            throw new IllegalArgumentException("O cache precisa de pelo menos uma entrada");
        }
        // Ordem de inserção: a mais antiga é a primeira a sair quando o limite é passado.
        this.entradas = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Entrada> maisAntiga) {
                return size() > tamanhoMaximo;
            }
        };
    }

    @Override
    public synchronized List<Captura> buscar(String appId) {
        Instant agora = relogio.instant();
        Entrada atual = entradas.get(appId);
        if (atual != null && agora.isBefore(atual.obtidaEm().plus(validade))) {
            return atual.capturas();
        }
        try {
            List<Captura> capturas = List.copyOf(real.buscar(appId));
            entradas.remove(appId); // regravar coloca a entrada no fim da fila de descarte
            entradas.put(appId, new Entrada(capturas, agora));
            return capturas;
        } catch (RuntimeException falha) {
            return atual != null ? atual.capturas() : List.of();
        }
    }
}
