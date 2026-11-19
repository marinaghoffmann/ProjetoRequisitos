package com.g4.game_tracker_backend.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.aplicacao.catalogo.BuscadorDeCapturas;
import com.g4.game_tracker_backend.aplicacao.catalogo.BuscadorDeCapturas.Captura;
import com.g4.game_tracker_backend.aplicacao.catalogo.CapturasEmCache;

/** Padrão Proxy (de cache) da F-01. */
class CapturasEmCacheTest {

    private static final Duration VALIDADE = Duration.ofHours(24);
    private static final Instant T0 = Instant.parse("2026-11-19T12:00:00Z");

    /** Relógio que o teste avança. */
    private static final class RelogioAjustavel extends Clock {
        private Instant agora = T0;
        void avancar(Duration tempo) { agora = agora.plus(tempo); }
        @Override public Instant instant() { return agora; }
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zona) { return this; }
    }

    /** Buscador real falso: conta as chamadas por appId e pode passar a falhar. */
    private static final class BuscadorFalso implements BuscadorDeCapturas {
        final Map<String, Integer> chamadas = new HashMap<>();
        boolean falhar;
        @Override public List<Captura> buscar(String appId) {
            chamadas.merge(appId, 1, Integer::sum);
            if (falhar) throw new IllegalStateException("Steam fora do ar");
            return List.of(new Captura(appId + "-mini-" + chamadas.get(appId), appId + "-full"));
        }
    }

    private final RelogioAjustavel relogio = new RelogioAjustavel();
    private final BuscadorFalso real = new BuscadorFalso();

    @Test
    void segundaConsultaDoMesmoAppIdNaoChamaOReal() {
        CapturasEmCache cache = new CapturasEmCache(real, relogio, VALIDADE);
        List<Captura> primeira = cache.buscar("292030");
        relogio.avancar(Duration.ofHours(23));
        assertEquals(primeira, cache.buscar("292030"));
        assertEquals(1, real.chamadas.get("292030"));
    }

    @Test
    void depoisDaValidadeChamaORealDeNovo() {
        CapturasEmCache cache = new CapturasEmCache(real, relogio, VALIDADE);
        cache.buscar("292030");
        relogio.avancar(VALIDADE);
        assertEquals("292030-mini-2", cache.buscar("292030").get(0).miniatura());
        assertEquals(2, real.chamadas.get("292030"));
    }

    @Test
    void falhaDoRealComEntradaVencidaDevolveAEntradaVencida() {
        CapturasEmCache cache = new CapturasEmCache(real, relogio, VALIDADE);
        List<Captura> guardada = cache.buscar("292030");
        relogio.avancar(VALIDADE.plusHours(1));
        real.falhar = true;
        assertEquals(guardada, cache.buscar("292030"));
        assertEquals(2, real.chamadas.get("292030"));
    }

    @Test
    void falhaDoRealSemEntradaDevolveListaVazia() {
        real.falhar = true;
        assertTrue(new CapturasEmCache(real, relogio, VALIDADE).buscar("1").isEmpty());
    }

    @Test
    void limiteDeTamanhoDescartaAEntradaMaisAntiga() {
        CapturasEmCache cache = new CapturasEmCache(real, relogio, VALIDADE, 2);
        cache.buscar("a");
        cache.buscar("b");
        cache.buscar("c"); // passa do limite: "a" sai
        cache.buscar("b");
        cache.buscar("a");
        assertEquals(1, real.chamadas.get("b"));
        assertEquals(2, real.chamadas.get("a"));
    }
}
