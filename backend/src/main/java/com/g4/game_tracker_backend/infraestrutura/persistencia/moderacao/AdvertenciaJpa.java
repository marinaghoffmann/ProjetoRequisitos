package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;
@Entity @Table(name="moderacao_advertencias",indexes=@Index(name="ix_advertencias_usuario",columnList="usuario_id"))
public class AdvertenciaJpa {
 @Id public UUID id;
 @Column(name="usuario_id",nullable=false) public UUID usuarioId;
 @Column(name="moderador_id",nullable=false) public UUID moderadorId;
 @Column(nullable=false,length=1000) public String motivo;
 @Column(name="registrada_em",nullable=false) public Instant registradaEm;
 protected AdvertenciaJpa() {}
 public AdvertenciaJpa(UUID usuario,UUID moderador,String motivo,Instant agora) { id=UUID.randomUUID();usuarioId=usuario;moderadorId=moderador;this.motivo=motivo;registradaEm=agora; }
}
