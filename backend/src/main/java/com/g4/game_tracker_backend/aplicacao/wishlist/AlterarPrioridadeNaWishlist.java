package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.wishlist.PrioridadeWishlist;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

@CasoDeUso
public class AlterarPrioridadeNaWishlist {

    private final IdentidadeAutenticada identidade;
    private final WishlistRepositorio wishlists;

    public AlterarPrioridadeNaWishlist(IdentidadeAutenticada identidade, WishlistRepositorio wishlists) {
        this.identidade = identidade;
        this.wishlists = wishlists;
    }

    @Transacional
    public void executar(UUID jogoId, PrioridadeWishlist prioridade) {
        var atual = wishlists.buscarPorDono(identidade.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Este jogo não está na sua wishlist"));
        wishlists.salvar(atual.alterarPrioridade(jogoId, prioridade));
    }
}