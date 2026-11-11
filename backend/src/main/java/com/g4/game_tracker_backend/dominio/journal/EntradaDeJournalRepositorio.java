package com.g4.game_tracker_backend.dominio.journal;

import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;

public interface EntradaDeJournalRepositorio {
    EntradaDeJournal salvar(EntradaDeJournal entrada);
    Optional<EntradaDeJournal> buscarPorId(UUID id);
    /** A entrada automática gerada por uma ação de origem (F-16 RN 2). */
    Optional<EntradaDeJournal> buscarPorOrigem(TipoEventoPlataforma evento, String origemId);
    /** Timeline do jogador, manuais e automáticas, da sessão mais recente para a mais antiga. Página a partir de 1. */
    Pagina<EntradaDeJournal> listarTimeline(UUID donoId, int pagina, int tamanho);
    void remover(UUID id);
}
