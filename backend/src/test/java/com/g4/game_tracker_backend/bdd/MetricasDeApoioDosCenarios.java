package com.g4.game_tracker_backend.bdd;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeConclusoes;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeWishlists;

/**
 * Faz o papel de F-06 e F-11 nos cenários da F-17 enquanto elas não existem. Ao implementar a fonte real
 * de uma delas, remova daqui a porta correspondente e use a funcionalidade nos steps. As reviews (F-02)
 * já são reais: os steps publicam reviews de verdade e ReviewsParaEstatisticas entrega as notas.
 */
@Component
public class MetricasDeApoioDosCenarios implements FonteDeConclusoes, FonteDeWishlists {

    private final Map<UUID, Integer> conclusoes = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> wishlists = new ConcurrentHashMap<>();

    void somarConclusoes(UUID jogoId, int quantidade) {
        conclusoes.merge(jogoId, quantidade, Integer::sum);
    }

    void somarWishlists(UUID jogoId, int quantidade) {
        wishlists.merge(jogoId, quantidade, Integer::sum);
    }

    @Override
    public int contarConclusoes(UUID jogoId) {
        return conclusoes.getOrDefault(jogoId, 0);
    }

    @Override
    public int contarPresencaEmWishlists(UUID jogoId) {
        return wishlists.getOrDefault(jogoId, 0);
    }
}
