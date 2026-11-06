package com.g4.game_tracker_backend.dominio.avaliacao.estatisticas;

import java.util.UUID;

/** Porta para os dados da F-06. Enquanto não houver implementação, o total fica zerado. */
public interface FonteDeWishlists {
    int contarPresencaEmWishlists(UUID jogoId);
}
