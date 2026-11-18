package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;
import java.time.Instant;
import java.util.UUID;
import com.g4.game_tracker_backend.dominio.moderacao.*;
import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
@Entity @Immutable @Table(name="moderacao_logs",indexes=@Index(name="ix_moderacao_logs_data",columnList="registrado_em"))
public class LogModeracaoJpa {
 @Id public UUID id;
 @Column(name="moderador_id",nullable=false) public UUID moderadorId;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) public AcaoModeracao acao;
 @Enumerated(EnumType.STRING) @Column(length=32) public TipoConteudo tipo;
 @Column(name="conteudo_id") public UUID conteudoId;
 @Column(length=2000) public String justificativa;
 @Column(name="registrado_em",nullable=false) public Instant registradoEm;
 protected LogModeracaoJpa() {}
 public LogModeracaoJpa(UUID moderador,AcaoModeracao acao,TipoConteudo tipo,UUID conteudoId,String justificativa,Instant agora) { id=UUID.randomUUID();moderadorId=moderador;this.acao=acao;this.tipo=tipo;this.conteudoId=conteudoId;this.justificativa=justificativa;registradoEm=agora; }
}
