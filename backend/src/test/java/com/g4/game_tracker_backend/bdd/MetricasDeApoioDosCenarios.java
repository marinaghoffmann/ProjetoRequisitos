package com.g4.game_tracker_backend.bdd;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeWishlists;

/**
 * Faz o papel da wishlist (F-06) nos cenários da F-17 até a fonte real estar disponível.
 */
@Component
public class MetricasDeApoioDosCenarios implements FonteDeWishlists {

    private final Map<UUID, Integer> wishlists = new ConcurrentHashMap<>();

    void somarWishlists(UUID jogoId, int quantidade) {
        wishlists.merge(jogoId, quantidade, Integer::sum);
    }

    @Override
    public int contarPresencaEmWishlists(UUID jogoId) {
        return wishlists.getOrDefault(jogoId, 0);
    }
}
