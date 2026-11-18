package com.g4.game_tracker_backend.dominio.journal;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

public record FiltroJournal(UUID jogoId, String tag, LocalDate inicio, LocalDate fim, TipoOrigem origem) {
    public FiltroJournal {
        tag = tag == null || tag.isBlank() ? null : tag.strip().replaceFirst("^#", "").toLowerCase(Locale.ROOT);
        if (tag != null && tag.length() > 40) throw new RegraDeNegocioException("Cada tag deve ter no máximo 40 caracteres");
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new RegraDeNegocioException("A data inicial não pode ser posterior à data final");
        }
    }

    public static FiltroJournal todos() { return new FiltroJournal(null, null, null, null, null); }
}
