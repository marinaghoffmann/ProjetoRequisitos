package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.MetricasDoJogoAlteradas;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

/** Remove o jogo da wishlist e, se ele estava lá, avisa a F-17 para recalcular a ficha. */
@Service
public class RemoverDaWishlist {

    private final IdentidadeAutenticada identidade;
    private final WishlistRepositorio wishlist;
    private final ApplicationEventPublisher eventos;

    public RemoverDaWishlist(IdentidadeAutenticada identidade, WishlistRepositorio wishlist,
            ApplicationEventPublisher eventos) {
        this.identidade = identidade;
        this.wishlist = wishlist;
        this.eventos = eventos;
    }

    @Transactional
    public void executar(UUID jogoId) {
        UUID usuarioId = identidade.usuarioId();
        if (!wishlist.contem(usuarioId, jogoId)) {
            return;
        }
        wishlist.remover(usuarioId, jogoId);
        eventos.publishEvent(new MetricasDoJogoAlteradas(jogoId));
    }
}
