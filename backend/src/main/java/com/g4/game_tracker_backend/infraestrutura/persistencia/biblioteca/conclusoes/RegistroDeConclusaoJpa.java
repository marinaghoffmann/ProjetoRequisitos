package com.g4.game_tracker_backend.infraestrutura.persistencia.biblioteca.conclusoes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "registros_conclusao", indexes = @Index(name = "idx_conclusao_jogo_jogador_data",
        columnList = "jogo_id, jogador_id, data_conclusao"))
public class RegistroDeConclusaoJpa {

    @Id
    private UUID id;
    @Column(name = "jogador_id", nullable = false, updatable = false)
    private UUID jogadorId;
    @Column(name = "jogo_id", nullable = false)
    private UUID jogoId;
    @Column(name = "data_conclusao", nullable = false)
    private LocalDate dataConclusao;
    @Column(length = 100)
    private String plataforma;
    @Column(precision = 19, scale = 6)
    private BigDecimal horasJogadas;
    @Column(nullable = false)
    private boolean concluido;
    @Column(nullable = false)
    private boolean platina;
    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    protected RegistroDeConclusaoJpa() {
    }

    RegistroDeConclusaoJpa(RegistroDeConclusao registro) {
        id = registro.id();
        jogadorId = registro.jogadorId();
        registradoEm = registro.registradoEm();
        atualizar(registro);
    }

    void atualizar(RegistroDeConclusao registro) {
        jogoId = registro.jogoId();
        dataConclusao = registro.dataConclusao();
        plataforma = registro.plataforma();
        horasJogadas = registro.horasJogadas();
        concluido = registro.concluido();
        platina = registro.platina();
    }

    RegistroDeConclusao paraDominio() {
        return new RegistroDeConclusao(id, jogadorId, jogoId, dataConclusao, plataforma, horasJogadas,
                concluido, platina, registradoEm);
    }
}
