package com.g4.game_tracker_backend.infraestrutura.persistencia.reputacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.marketplace.reputacao.ReputacaoUsuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Uma linha por usuário avaliado. A nota média é gravada já calculada (duas casas), além da soma das estrelas. */
@Entity
@Table(name = "marketplace_reputacoes")
public class ReputacaoUsuarioJpa {

    @Id
    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "nota_media", precision = 3, scale = 2)
    private BigDecimal notaMedia;

    @Column(name = "soma_estrelas", nullable = false)
    private int somaEstrelas;

    @Column(name = "total_avaliacoes", nullable = false)
    private int totalAvaliacoes;

    @Column(name = "negociacoes_bem_sucedidas", nullable = false)
    private int negociacoesBemSucedidas;

    @Column(name = "atualizada_em")
    private Instant atualizadaEm;

    protected ReputacaoUsuarioJpa() {
    }

    private ReputacaoUsuarioJpa(ReputacaoUsuario reputacao) {
        this.usuarioId = reputacao.usuarioId();
        this.notaMedia = reputacao.notaMedia();
        this.somaEstrelas = reputacao.somaEstrelas();
        this.totalAvaliacoes = reputacao.totalAvaliacoes();
        this.negociacoesBemSucedidas = reputacao.negociacoesBemSucedidas();
        this.atualizadaEm = reputacao.atualizadaEm();
    }

    static ReputacaoUsuarioJpa deDominio(ReputacaoUsuario reputacao) {
        return new ReputacaoUsuarioJpa(reputacao);
    }

    ReputacaoUsuario paraDominio() {
        return ReputacaoUsuario.reconstituir(usuarioId, somaEstrelas, totalAvaliacoes, negociacoesBemSucedidas,
                atualizadaEm);
    }
}
