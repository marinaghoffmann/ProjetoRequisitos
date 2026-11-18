package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

@Service
public class AdicionarNaWishlist {

    private final IdentidadeAutenticada identidade;
    private final WishlistRepositorio wishlist;
    private final JogoRepositorio jogos;

    public AdicionarNaWishlist(IdentidadeAutenticada identidade, WishlistRepositorio wishlist,
            JogoRepositorio jogos) {
        this.identidade = identidade;
        this.wishlist = wishlist;
        this.jogos = jogos;
    }

    @Transactional
    public void executar(UUID jogoId) {
        if (jogos.buscarPorId(jogoId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Jogo não encontrado");
        }
        wishlist.adicionar(identidade.usuarioId(), jogoId);
    }
}