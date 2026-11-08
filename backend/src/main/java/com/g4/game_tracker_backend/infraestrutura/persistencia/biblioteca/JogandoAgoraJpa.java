package com.g4.game_tracker_backend.infraestrutura.persistencia.biblioteca;

import java.time.LocalDate;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.biblioteca.JogandoAgora;
import com.g4.game_tracker_backend.dominio.biblioteca.ProgressoJogatina;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "jogando_agora")
class JogandoAgoraJpa {

    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID usuarioId;
    @Column(nullable = false)
    private UUID jogoId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressoJogatina progresso;
    @Column(nullable = false)
    private LocalDate dataInicio;
    @Column(nullable = false)
    private boolean ativo;

    protected JogandoAgoraJpa() {
    }

    static JogandoAgoraJpa de(JogandoAgora d) {
        JogandoAgoraJpa j = new JogandoAgoraJpa();
        j.id = d.id();
        j.usuarioId = d.usuarioId();
        j.jogoId = d.jogoId();
        j.progresso = d.progresso();
        j.dataInicio = d.dataInicio();
        j.ativo = d.ativo();
        return j;
    }

    JogandoAgora paraDominio() {
        return JogandoAgora.reconstituir(id, usuarioId, jogoId, progresso, dataInicio, ativo);
    }
}