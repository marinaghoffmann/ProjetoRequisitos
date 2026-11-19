package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

@CasoDeUso
public class ConsultarWishlist {

    private final IdentidadeAutenticada identidade;
    private final WishlistRepositorio wishlists;
    private final JogoRepositorio jogos;

    public ConsultarWishlist(IdentidadeAutenticada identidade, WishlistRepositorio wishlists,
            JogoRepositorio jogos) {
        this.identidade = identidade;
        this.wishlists = wishlists;
        this.jogos = jogos;
    }

    public List<ItemWishlistDetalhado> doUsuarioAtual() {
        return executar(identidade.usuarioId());
    }

    /** Ordenada por prioridade. Sem wishlist, devolve lista vazia. */
    @Transacional(somenteLeitura = true)
    public List<ItemWishlistDetalhado> executar(UUID donoId) {
        return wishlists.buscarPorDono(donoId).stream()
                .flatMap(w -> w.itensOrdenados().stream())
                .flatMap(item -> jogos.buscarPorId(item.jogoId())
                        .map(jogo -> new ItemWishlistDetalhado(jogo, item.prioridade(), item.adicionadoEm()))
                        .stream())
                .toList();
    }
}