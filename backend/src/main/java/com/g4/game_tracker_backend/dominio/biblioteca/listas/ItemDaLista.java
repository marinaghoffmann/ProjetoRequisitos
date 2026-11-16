package com.g4.game_tracker_backend.dominio.biblioteca.listas;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Jogo dentro de uma lista personalizada, com a nota explicativa opcional de quem montou a lista. */
public record ItemDaLista(UUID id, UUID jogoId, String notaExplicativa, Instant incluidoEm) {

    public ItemDaLista {
        Objects.requireNonNull(id, "O identificador do item é obrigatório");
        Objects.requireNonNull(jogoId, "O jogo do item é obrigatório");
        Objects.requireNonNull(incluidoEm, "A data de inclusão é obrigatória");
        notaExplicativa = notaExplicativa == null ? "" : notaExplicativa;
    }

    ItemDaLista comNota(String novaNota) {
        return new ItemDaLista(id, jogoId, novaNota, incluidoEm);
    }
}
