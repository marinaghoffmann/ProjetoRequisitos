package com.g4.game_tracker_backend.infraestrutura.persistencia.biblioteca.acervo;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.biblioteca.acervo.ItemBiblioteca;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "biblioteca_itens",
        uniqueConstraints = @UniqueConstraint(name = "uk_biblioteca_usuario_jogo", columnNames = { "usuario_id", "jogo_id" }))
public class ItemBibliotecaJpa {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "jogo_id", nullable = false)
    private UUID jogoId;

    @Column(name = "adicionado_em", nullable = false)
    private Instant adicionadoEm;

    protected ItemBibliotecaJpa() {
    }

    static ItemBibliotecaJpa deDominio(ItemBiblioteca i) {
        ItemBibliotecaJpa e = new ItemBibliotecaJpa();
        e.id = i.id();
        e.usuarioId = i.usuarioId();
        e.jogoId = i.jogoId();
        e.adicionadoEm = i.adicionadoEm();
        return e;
    }

    ItemBiblioteca paraDominio() {
        return ItemBiblioteca.reconstituir(id, usuarioId, jogoId, adicionadoEm);
    }
}