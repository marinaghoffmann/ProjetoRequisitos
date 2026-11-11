package com.g4.game_tracker_backend.infraestrutura.persistencia.pedidos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.marketplace.pedidos.Carteira;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Uma linha por usuário, criada na primeira transação dele (RN 6). */
@Entity
@Table(name = "marketplace_carteiras")
public class CarteiraJpa {

    @Id
    @Column(name = "dono_id")
    private UUID donoId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal saldo;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private Instant atualizadaEm;

    protected CarteiraJpa() {
    }

    private CarteiraJpa(Carteira carteira) {
        this.donoId = carteira.donoId();
        this.saldo = carteira.saldo();
        this.criadaEm = carteira.criadaEm();
        this.atualizadaEm = carteira.atualizadaEm();
    }

    static CarteiraJpa deDominio(Carteira carteira) {
        return new CarteiraJpa(carteira);
    }

    Carteira paraDominio() {
        return Carteira.reconstituir(donoId, saldo, criadaEm, atualizadaEm);
    }
}
