package com.g4.game_tracker_backend.infraestrutura.persistencia.negociacao;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.marketplace.negociacao.MensagemChat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "marketplace_mensagens")
public class MensagemChatJpa {

    @Id
    private UUID id;

    @Column(name = "remetente_id", nullable = false)
    private UUID remetenteId;

    @Column(nullable = false, length = MensagemChat.LIMITE_DO_TEXTO)
    private String texto;

    @Column(name = "enviada_em", nullable = false)
    private Instant enviadaEm;

    @Column(name = "lida_em")
    private Instant lidaEm;

    protected MensagemChatJpa() {
    }

    private MensagemChatJpa(MensagemChat mensagem) {
        this.id = mensagem.id();
        this.remetenteId = mensagem.remetenteId();
        this.texto = mensagem.texto();
        this.enviadaEm = mensagem.enviadaEm();
        this.lidaEm = mensagem.lidaEm();
    }

    static MensagemChatJpa deDominio(MensagemChat mensagem) {
        return new MensagemChatJpa(mensagem);
    }

    MensagemChat paraDominio() {
        return MensagemChat.reconstituir(id, remetenteId, texto, enviadaEm, lidaEm);
    }

    Instant enviadaEm() {
        return enviadaEm;
    }
}
