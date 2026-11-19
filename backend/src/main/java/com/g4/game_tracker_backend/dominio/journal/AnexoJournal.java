package com.g4.game_tracker_backend.dominio.journal;

import java.util.Objects;
import java.util.UUID;

/** Metadados da imagem pertencente à entrada. O conteúdo binário fica na infraestrutura. */
public record AnexoJournal(UUID id, String nomeArquivo, String tipoMidia, long tamanho) {
    public AnexoJournal {
        Objects.requireNonNull(id);
        Objects.requireNonNull(nomeArquivo);
        Objects.requireNonNull(tipoMidia);
    }
}
