package com.g4.game_tracker_backend.apresentacao.compartilhado;

import java.util.List;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;

public record PaginaResponse<T>(List<T> itens, int pagina, int tamanho, long totalItens, int totalPaginas) {

    public static <T> PaginaResponse<T> de(Pagina<T> pagina) {
        return new PaginaResponse<>(pagina.itens(), pagina.pagina(), pagina.tamanho(), pagina.totalItens(),
                pagina.totalPaginas());
    }
}
