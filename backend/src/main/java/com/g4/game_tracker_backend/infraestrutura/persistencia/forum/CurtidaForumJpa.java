package com.g4.game_tracker_backend.infraestrutura.persistencia.forum;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Uma curtida por usuário e postagem, vinculada ao tópico para contagem e exclusão lógica. */
@Entity
@Table(name = "forum_curtidas", uniqueConstraints = @UniqueConstraint(name = "uk_forum_curtida_usuario_conteudo",
        columnNames = {"usuario_id", "tipo", "conteudo_id"}), indexes =
        @Index(name = "ix_forum_curtidas_topico", columnList = "topico_id"))
public class CurtidaForumJpa {
    @Id
    private UUID id;
    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;
    @Column(name = "topico_id", nullable = false)
    private UUID topicoId;
    @Column(nullable = false, length = 8)
    private String tipo;
    @Column(name = "conteudo_id", nullable = false)
    private UUID conteudoId;

    protected CurtidaForumJpa() {}

    public CurtidaForumJpa(UUID usuarioId, UUID topicoId, String tipo, UUID conteudoId) {
        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
        this.topicoId = topicoId;
        this.tipo = tipo;
        this.conteudoId = conteudoId;
    }

    public UUID usuarioId() { return usuarioId; }
    public String tipo() { return tipo; }
    public UUID conteudoId() { return conteudoId; }
}
