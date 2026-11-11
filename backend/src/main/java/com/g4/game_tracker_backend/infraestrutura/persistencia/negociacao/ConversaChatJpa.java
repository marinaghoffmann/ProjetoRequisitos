package com.g4.game_tracker_backend.infraestrutura.persistencia.negociacao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.BatchSize;

import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Conversa com as mensagens e as propostas. A restrição única (anuncio_id, comprador_id) garante a RN 2
 * mesmo com duas requisições simultâneas; o vendedor é sempre o dono do anúncio.
 */
@Entity
@Table(name = "marketplace_conversas", indexes = {
        @Index(name = "ix_marketplace_conversas_comprador", columnList = "comprador_id"),
        @Index(name = "ix_marketplace_conversas_vendedor", columnList = "vendedor_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_marketplace_conversas_anuncio_comprador",
                columnNames = { "anuncio_id", "comprador_id" })
})
public class ConversaChatJpa {

    @Id
    private UUID id;

    @Column(name = "anuncio_id", nullable = false)
    private UUID anuncioId;

    @Column(name = "comprador_id", nullable = false)
    private UUID compradorId;

    @Column(name = "vendedor_id", nullable = false)
    private UUID vendedorId;

    @Column(name = "iniciada_em", nullable = false)
    private Instant iniciadaEm;

    @Column(name = "ultima_atividade_em", nullable = false)
    private Instant ultimaAtividadeEm;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversa_id", nullable = false)
    @BatchSize(size = 50)
    private List<MensagemChatJpa> mensagens = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversa_id", nullable = false)
    @BatchSize(size = 50)
    private List<PropostaNegociacaoJpa> propostas = new ArrayList<>();

    protected ConversaChatJpa() {
    }

    private ConversaChatJpa(ConversaChat conversa) {
        this.id = conversa.id();
        this.anuncioId = conversa.anuncioId();
        this.compradorId = conversa.compradorId();
        this.vendedorId = conversa.vendedorId();
        this.iniciadaEm = conversa.iniciadaEm();
        this.ultimaAtividadeEm = conversa.ultimaAtividadeEm();
        this.mensagens = new ArrayList<>(conversa.mensagens().stream().map(MensagemChatJpa::deDominio).toList());
        this.propostas = new ArrayList<>(conversa.propostas().stream().map(PropostaNegociacaoJpa::deDominio).toList());
    }

    static ConversaChatJpa deDominio(ConversaChat conversa) {
        return new ConversaChatJpa(conversa);
    }

    /** Carrega as coleções sob demanda: chame dentro da transação do caso de uso. */
    ConversaChat paraDominio() {
        return ConversaChat.reconstituir(id, anuncioId, compradorId, vendedorId, iniciadaEm, ultimaAtividadeEm,
                mensagens.stream()
                        .sorted(Comparator.comparing(MensagemChatJpa::enviadaEm))
                        .map(MensagemChatJpa::paraDominio)
                        .toList(),
                propostas.stream()
                        .sorted(Comparator.comparing(PropostaNegociacaoJpa::enviadaEm))
                        .map(PropostaNegociacaoJpa::paraDominio)
                        .toList());
    }
}
