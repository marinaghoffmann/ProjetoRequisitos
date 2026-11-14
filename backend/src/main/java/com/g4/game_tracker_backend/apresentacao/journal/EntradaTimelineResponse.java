package com.g4.game_tracker_backend.apresentacao.journal;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;

/** Entrada da timeline. tipoEvento só vem preenchido nas automáticas; textoEditavel é falso para elas (RN 3). */
public record EntradaTimelineResponse(UUID id, UUID jogoId, String jogoTitulo, Instant dataSessao, String texto,
        boolean contemSpoiler, String tipoOrigem, String tipoEvento, boolean textoEditavel) {

    static EntradaTimelineResponse de(EntradaDeJournal entrada, Map<UUID, String> titulos) {
        return new EntradaTimelineResponse(entrada.id(), entrada.jogoId(), titulos.get(entrada.jogoId()),
                entrada.dataSessao(), entrada.texto(), entrada.contemSpoiler(), entrada.tipoOrigem().name(),
                entrada.tipoEvento() == null ? null : entrada.tipoEvento().name(), !entrada.automatica());
    }
}
