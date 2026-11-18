package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

@Service
public class ConsultarWishlist {

    private final IdentidadeAutenticada identidade;
    private final WishlistRepositorio wishlist;
    private final JogoRepositorio jogos;

    public ConsultarWishlist(IdentidadeAutenticada identidade, WishlistRepositorio wishlist,
            JogoRepositorio jogos) {
        this.identidade = identidade;
        this.wishlist = wishlist;
        this.jogos = jogos;
    }

    @Transactional(readOnly = true)
    public List<Jogo> executar() {
        return wishlist.jogoIdsDoUsuario(identidade.usuarioId()).stream()
                .map(jogos::buscarPorId)
                .flatMap(java.util.Optional::stream)
                .toList();
    }
}