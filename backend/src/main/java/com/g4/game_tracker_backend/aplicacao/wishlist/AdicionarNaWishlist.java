package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.wishlist.PrioridadeWishlist;
import com.g4.game_tracker_backend.dominio.wishlist.Wishlist;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

@Service
public class AdicionarNaWishlist {

    private final IdentidadeAutenticada identidade;
    private final WishlistRepositorio wishlists;
    private final JogoRepositorio jogos;

    public AdicionarNaWishlist(IdentidadeAutenticada identidade, WishlistRepositorio wishlists,
            JogoRepositorio jogos) {
        this.identidade = identidade;
        this.wishlists = wishlists;
        this.jogos = jogos;
    }

    @Transactional
    public void executar(UUID jogoId, PrioridadeWishlist prioridade) {
        Jogo jogo = jogos.buscarPorId(jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        if (jogo.status() != StatusCatalogo.ATIVO) {
            throw new RegraDeNegocioException("Este jogo não está disponível no catálogo");
        }
        UUID donoId = identidade.usuarioId();
        Instant agora = Instant.now();
        Wishlist atual = wishlists.buscarPorDono(donoId).orElseGet(() -> Wishlist.criar(donoId, agora));
        wishlists.salvar(atual.adicionar(jogoId, prioridade, agora));
    }
}