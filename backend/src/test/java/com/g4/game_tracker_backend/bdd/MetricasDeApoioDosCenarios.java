package com.g4.game_tracker_backend.bdd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeConclusoes;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeReviews;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeWishlists;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.NotasAgregadas;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.NotasDoJogo;

/**
 * Faz o papel de F-02, F-06 e F-11 nos cenários da F-17 enquanto elas não existem. Ao implementar a
 * fonte real de uma delas, remova daqui a porta correspondente e use a funcionalidade nos steps.
 */
@Component
public class MetricasDeApoioDosCenarios implements FonteDeReviews, FonteDeConclusoes, FonteDeWishlists {

    private final Map<UUID, List<Integer>> notas = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> conclusoes = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> wishlists = new ConcurrentHashMap<>();

    void adicionarNotas(UUID jogoId, List<Integer> novasNotas) {
        notas.computeIfAbsent(jogoId, id -> new ArrayList<>()).addAll(novasNotas);
    }

    void removerNota(UUID jogoId, int nota) {
        notas.get(jogoId).remove(Integer.valueOf(nota));
    }

    void somarConclusoes(UUID jogoId, int quantidade) {
        conclusoes.merge(jogoId, quantidade, Integer::sum);
    }

    void somarWishlists(UUID jogoId, int quantidade) {
        wishlists.merge(jogoId, quantidade, Integer::sum);
    }

    @Override
    public NotasDoJogo notasDoJogo(UUID jogoId) {
        int[] notasDoJogo = notas.getOrDefault(jogoId, List.of()).stream().mapToInt(Integer::intValue).toArray();
        NotasAgregadas vazias = NotasAgregadas.vazias();
        return new NotasDoJogo(NotasAgregadas.de(notasDoJogo), vazias, vazias, vazias, vazias, vazias);
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
