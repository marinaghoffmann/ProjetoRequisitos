package com.g4.game_tracker_backend.apresentacao.forum;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.forum.ResumoTopico;

public record TopicoResumoResponse(UUID id, UUID autorId, String nomeAutor, UUID jogoId, String categoria,
        String titulo, String status, int totalRespostas, Instant dataUltimaAtividade, Instant publicadoEm) {

    static TopicoResumoResponse de(ResumoTopico resumo, Map<UUID, String> nomes) {
        return new TopicoResumoResponse(resumo.id(), resumo.autorId(), nomes.get(resumo.autorId()), resumo.jogoId(),
                resumo.categoria(), resumo.titulo(), resumo.status().name(), resumo.totalRespostas(),
                resumo.dataUltimaAtividade(), resumo.publicadoEm());
    }
}
