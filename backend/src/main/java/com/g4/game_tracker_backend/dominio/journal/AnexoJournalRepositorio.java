package com.g4.game_tracker_backend.dominio.journal;

import java.util.Optional;
import java.util.UUID;

public interface AnexoJournalRepositorio {
    void adicionar(UUID entradaId, AnexoJournal anexo, byte[] conteudo);
    void remover(UUID entradaId, UUID anexoId);
    Optional<byte[]> conteudo(UUID entradaId, UUID anexoId);
}
