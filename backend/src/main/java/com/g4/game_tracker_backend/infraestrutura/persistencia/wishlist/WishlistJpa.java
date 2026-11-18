package com.g4.game_tracker_backend.infraestrutura.persistencia.wishlist;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "wishlist", uniqueConstraints = @UniqueConstraint(
        name = "uk_wishlist_usuario_jogo", columnNames = { "usuario_id", "jogo_id" }))
public class WishlistJpa {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "jogo_id", nullable = false)
    private UUID jogoId;

    @Column(name = "adicionado_em", nullable = false)
    private Instant adicionadoEm;

    protected WishlistJpa() {
    }

    WishlistJpa(UUID usuarioId, UUID jogoId) {
        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
        this.jogoId = jogoId;
        this.adicionadoEm = Instant.now();
    }

    UUID jogoId() { return jogoId; }
}