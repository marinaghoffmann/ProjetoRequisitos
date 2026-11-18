package com.g4.game_tracker_backend.aplicacao.lancamentos;
import java.time.*;
import java.time.temporal.ChronoUnit;
/** A data oficial é LocalDate; o dia começa no mesmo fuso adotado pelo marketplace. */
public final class DatasLancamentos {
    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private DatasLancamentos() {}
    public static LocalDate hoje(Instant agora) { return agora.atZone(FUSO).toLocalDate(); }
    public static Instant dispararEm(LocalDate lancamento) {
        return lancamento.minusDays(1).atStartOfDay(FUSO).toInstant();
    }
    public static long diasRestantes(LocalDate lancamento, Instant agora) {
        return Math.max(0, ChronoUnit.DAYS.between(hoje(agora), lancamento));
    }
}
