package com.g4.game_tracker_backend.dominio.avaliacao.reviews;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Notas de todas as reviews de um jogo, somadas: a nota geral e cada categoria. */
public record ResumoDeNotas(SomaDeNotas geral, Map<CategoriaReview, SomaDeNotas> porCategoria) {

    public ResumoDeNotas {
        geral = geral == null ? SomaDeNotas.vazia() : geral;
        Map<CategoriaReview, SomaDeNotas> copia = new EnumMap<>(CategoriaReview.class);
        if (porCategoria != null) {
            copia.putAll(porCategoria);
        }
        porCategoria = Map.copyOf(copia);
    }

    public static ResumoDeNotas vazio() {
        return new ResumoDeNotas(SomaDeNotas.vazia(), Map.of());
    }

    public SomaDeNotas daCategoria(CategoriaReview categoria) {
        return porCategoria.getOrDefault(categoria, SomaDeNotas.vazia());
    }

    /** Cada review tem exatamente uma nota geral. */
    public int totalReviews() {
        return geral.quantidade();
    }

    /** As cinco categorias, sempre na mesma ordem, inclusive as que ainda não têm nota. */
    public List<MediaCategoria> mediasPorCategoria() {
        return Arrays.stream(CategoriaReview.values())
                .map(categoria -> new MediaCategoria(categoria, daCategoria(categoria).media(),
                        daCategoria(categoria).quantidade()))
                .toList();
    }
}
