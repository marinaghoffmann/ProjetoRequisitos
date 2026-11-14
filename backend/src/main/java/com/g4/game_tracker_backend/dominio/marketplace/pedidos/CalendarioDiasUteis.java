package com.g4.game_tracker_backend.dominio.marketplace.pedidos;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** F-20 RN 8: dias úteis são de segunda a sexta, no horário de Brasília; feriados não são considerados. */
public final class CalendarioDiasUteis {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private CalendarioDiasUteis() {
    }

    /** Avança dia a dia, contando só os dias úteis, e mantém o horário de partida. */
    public static Instant somarDiasUteis(Instant inicio, int dias) {
        ZonedDateTime data = inicio.atZone(FUSO);
        int contados = 0;
        while (contados < dias) {
            data = data.plusDays(1);
            if (data.getDayOfWeek() != DayOfWeek.SATURDAY && data.getDayOfWeek() != DayOfWeek.SUNDAY) {
                contados++;
            }
        }
        return data.toInstant();
    }
}
