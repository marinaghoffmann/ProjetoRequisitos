package com.g4.game_tracker_backend.infraestrutura.persistencia.wishlist;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.g4.game_tracker_backend.dominio.wishlist.ItemWishlist;
import com.g4.game_tracker_backend.dominio.wishlist.Wishlist;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** RN 1: a chave primária é o dono, então o banco garante uma wishlist por usuário. */
@Entity
@Table(name = "wishlists")
public class WishlistJpa {

    @Id
    @Column(name = "dono_id")
    private UUID donoId;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @ElementCollection
    @CollectionTable(name = "itens_wishlist",
            joinColumns = @JoinColumn(name = "dono_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_itens_wishlist_dono_jogo",
                    columnNames = { "dono_id", "jogo_id" }))
    private Set<ItemWishlistJpa> itens = new LinkedHashSet<>();

    protected WishlistJpa() {
    }

    WishlistJpa(UUID donoId, Instant criadaEm) {
        this.donoId = donoId;
        this.criadaEm = criadaEm;
    }

    /** Aplica só a diferença, para o Hibernate não apagar e reinserir a lista inteira. */
    void sincronizar(java.util.List<ItemWishlist> itensDoDominio) {
        Set<ItemWishlistJpa> novos = itensDoDominio.stream().map(ItemWishlistJpa::deDominio)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        itens.retainAll(novos);
        itens.addAll(novos);
    }

    Wishlist paraDominio() {
        return Wishlist.reconstituir(donoId, criadaEm,
                itens.stream().map(ItemWishlistJpa::paraDominio).toList());
    }
}