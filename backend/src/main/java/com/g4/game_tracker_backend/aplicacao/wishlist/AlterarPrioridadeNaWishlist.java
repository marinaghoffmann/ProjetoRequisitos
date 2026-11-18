package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.wishlist.PrioridadeWishlist;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

@Service
public class AlterarPrioridadeNaWishlist {

    private final IdentidadeAutenticada identidade;
    private final WishlistRepositorio wishlists;

    public AlterarPrioridadeNaWishlist(IdentidadeAutenticada identidade, WishlistRepositorio wishlists) {
        this.identidade = identidade;
        this.wishlists = wishlists;
    }

    @Transactional
    public void executar(UUID jogoId, PrioridadeWishlist prioridade) {
        var atual = wishlists.buscarPorDono(identidade.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Este jogo não está na sua wishlist"));
        wishlists.salvar(atual.alterarPrioridade(jogoId, prioridade));
    }
}