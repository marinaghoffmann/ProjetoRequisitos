package com.g4.game_tracker_backend.dominio.compartilhado;

import java.util.List;
import java.util.function.Function;

/** Uma página de resultados. A numeração começa em 1, como nas telas e nos cenários. */
public record Pagina<T>(List<T> itens, int pagina, int tamanho, long totalItens) {

    public Pagina {
        itens = List.copyOf(itens);
    }

    public int totalPaginas() {
        return tamanho == 0 ? 0 : (int) Math.ceil((double) totalItens / tamanho);
    }

    public <R> Pagina<R> map(Function<T, R> conversor) {
        return new Pagina<>(itens.stream().map(conversor).toList(), pagina, tamanho, totalItens);
    }
}
