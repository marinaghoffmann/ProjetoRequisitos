package com.g4.game_tracker_backend.infraestrutura.persistencia.wishlist;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.wishlist.ItemWishlist;
import com.g4.game_tracker_backend.dominio.wishlist.PrioridadeWishlist;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class ItemWishlistJpa {

    @Column(name = "jogo_id", nullable = false)
    private UUID jogoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PrioridadeWishlist prioridade;

    @Column(name = "adicionado_em", nullable = false)
    private Instant adicionadoEm;

    protected ItemWishlistJpa() {
    }

    private ItemWishlistJpa(ItemWishlist item) {
        this.jogoId = item.jogoId();
        this.prioridade = item.prioridade();
        this.adicionadoEm = item.adicionadoEm();
    }

    static ItemWishlistJpa deDominio(ItemWishlist item) {
        return new ItemWishlistJpa(item);
    }

    ItemWishlist paraDominio() {
        return new ItemWishlist(jogoId, prioridade, adicionadoEm);
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof ItemWishlistJpa i && Objects.equals(jogoId, i.jogoId)
                && prioridade == i.prioridade && Objects.equals(adicionadoEm, i.adicionadoEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jogoId, prioridade, adicionadoEm);
    }
}