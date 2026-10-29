package com.g4.game_tracker_backend.apresentacao.catalogo;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.catalogo.Jogo;

public record JogoResponse(UUID id, String titulo, String tituloOriginal, String slug, String resumo,
        String identificadorExterno, String origem, String status, UUID desenvolvedoraId, Set<UUID> generosIds,
        LocalDate dataLancamento, String capaUrl, String trailerUrl, String metadadosJson) {

    static JogoResponse de(Jogo jogo) {
        return new JogoResponse(jogo.id(), jogo.titulo(), jogo.tituloOriginal(), jogo.slug(), jogo.resumo(),
                jogo.identificadorExterno(), jogo.origem().name(), jogo.status().name(), jogo.desenvolvedoraId(),
                jogo.generosIds(), jogo.dataLancamento(), jogo.capaUrl(), jogo.trailerUrl(), jogo.metadadosJson());
    }
}
