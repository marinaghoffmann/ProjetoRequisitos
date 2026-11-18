package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;
import java.time.Instant;
import java.util.UUID;
import com.g4.game_tracker_backend.dominio.moderacao.*;
import jakarta.persistence.*;
@Entity @Table(name="moderacao_denuncias",indexes={@Index(name="ix_denuncias_status_data",columnList="status,registrada_em"),@Index(name="ix_denuncias_alvo",columnList="tipo,conteudo_id")})
public class DenunciaJpa {
 @Id public UUID id;
 @Column(name="denunciante_id",nullable=false) public UUID denuncianteId;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) public TipoConteudo tipo;
 @Column(name="conteudo_id",nullable=false) public UUID conteudoId;
 @Column(nullable=false,length=1000) public String motivo;
 @Column(name="chave_pendente",unique=true,length=120) public String chavePendente;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) public StatusDenuncia status;
 @Column(name="registrada_em",nullable=false) public Instant registradaEm;
 @Column(name="analisada_em") public Instant analisadaEm;
 protected DenunciaJpa() {}
 public DenunciaJpa(UUID usuario,TipoConteudo tipo,UUID conteudoId,String motivo,Instant agora) { id=UUID.randomUUID();denuncianteId=usuario;this.tipo=tipo;this.conteudoId=conteudoId;this.motivo=motivo;status=StatusDenuncia.PENDENTE;chavePendente=usuario+":"+tipo+":"+conteudoId;registradaEm=agora; }
}
