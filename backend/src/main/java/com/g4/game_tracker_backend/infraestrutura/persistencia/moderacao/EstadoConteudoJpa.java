package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;
import java.time.Instant;
import java.util.UUID;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;
import jakarta.persistence.*;
@Entity @Table(name="moderacao_estados", uniqueConstraints=@UniqueConstraint(name="uk_moderacao_alvo",columnNames={"tipo","conteudo_id"}))
public class EstadoConteudoJpa {
 @Id public UUID id;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) public TipoConteudo tipo;
 @Column(name="conteudo_id",nullable=false) public UUID conteudoId;
 @Column(nullable=false) public boolean oculto;
 @Column(nullable=false) public boolean removido;
 @Column(name="aviso_18",nullable=false) public boolean aviso18;
 @Column(name="atualizado_em",nullable=false) public Instant atualizadoEm;
 @Column(name="status_anterior",length=16) public String statusAnterior;
 protected EstadoConteudoJpa() {}
 public EstadoConteudoJpa(TipoConteudo tipo, UUID conteudoId, Instant agora) { this.id=UUID.randomUUID(); this.tipo=tipo; this.conteudoId=conteudoId; this.atualizadoEm=agora; }
 public boolean publico() { return !oculto && !removido; }
}
