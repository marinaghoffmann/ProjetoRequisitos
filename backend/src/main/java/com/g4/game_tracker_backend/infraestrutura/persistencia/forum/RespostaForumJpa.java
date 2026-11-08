package com.g4.game_tracker_backend.infraestrutura.persistencia.forum;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.forum.RespostaForum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "forum_respostas")
public class RespostaForumJpa {

    @Id
    private UUID id;

    @Column(name = "autor_id", nullable = false)
    private UUID autorId;

    @Column(name = "resposta_pai_id")
    private UUID respostaPaiId;

    @Column(nullable = false, length = 5000)
    private String conteudo;

    @Column(nullable = false)
    private boolean removida;

    @Column(name = "publicada_em", nullable = false)
    private Instant publicadaEm;

    @Column(name = "editada_em")
    private Instant editadaEm;

    protected RespostaForumJpa() {
    }

    private RespostaForumJpa(RespostaForum resposta) {
        this.id = resposta.id();
        this.autorId = resposta.autorId();
        this.respostaPaiId = resposta.respostaPaiId();
        this.conteudo = resposta.conteudo();
        this.removida = resposta.removida();
        this.publicadaEm = resposta.publicadaEm();
        this.editadaEm = resposta.editadaEm();
    }

    static RespostaForumJpa deDominio(RespostaForum resposta) {
        return new RespostaForumJpa(resposta);
    }

    RespostaForum paraDominio() {
        return RespostaForum.reconstituir(id, autorId, respostaPaiId, conteudo, removida, publicadaEm, editadaEm);
    }

    Instant publicadaEm() {
        return publicadaEm;
    }
}
