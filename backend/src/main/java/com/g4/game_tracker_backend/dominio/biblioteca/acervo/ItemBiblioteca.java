package com.g4.game_tracker_backend.dominio.biblioteca.acervo;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Jogo que o usuário declara já possuir (entre os jogos cadastrados no catálogo). */
public final class ItemBiblioteca {

    private final UUID id;
    private final UUID usuarioId;
    private final UUID jogoId;
    private final Instant adicionadoEm;

    private ItemBiblioteca(UUID id, UUID usuarioId, UUID jogoId, Instant adicionadoEm) {
        this.id = Objects.requireNonNull(id);
        this.usuarioId = Objects.requireNonNull(usuarioId, "O usuário é obrigatório");
        this.jogoId = Objects.requireNonNull(jogoId, "O jogo é obrigatório");
        this.adicionadoEm = Objects.requireNonNull(adicionadoEm);
    }

    public static ItemBiblioteca adicionar(UUID usuarioId, UUID jogoId) {
        return new ItemBiblioteca(UUID.randomUUID(), usuarioId, jogoId, Instant.now());
    }

    public static ItemBiblioteca reconstituir(UUID id, UUID usuarioId, UUID jogoId, Instant adicionadoEm) {
        return new ItemBiblioteca(id, usuarioId, jogoId, adicionadoEm);
    }

    public UUID id() { return id; }
    public UUID usuarioId() { return usuarioId; }
    public UUID jogoId() { return jogoId; }
    public Instant adicionadoEm() { return adicionadoEm; }
}