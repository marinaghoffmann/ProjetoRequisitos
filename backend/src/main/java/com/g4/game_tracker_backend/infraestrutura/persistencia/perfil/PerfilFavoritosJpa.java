package com.g4.game_tracker_backend.infraestrutura.persistencia.perfil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "perfis_favoritos")
class PerfilFavoritosJpa {
    @Id
    UUID donoId;

    @ElementCollection
    @CollectionTable(name = "perfil_jogos_favoritos", joinColumns = @JoinColumn(name = "dono_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"dono_id", "jogo_id"}))
    @OrderColumn(name = "posicao")
    @Column(name = "jogo_id", nullable = false)
    List<UUID> jogos = new ArrayList<>();

    protected PerfilFavoritosJpa() {
    }

    PerfilFavoritosJpa(UUID donoId) {
        this.donoId = donoId;
    }
}
