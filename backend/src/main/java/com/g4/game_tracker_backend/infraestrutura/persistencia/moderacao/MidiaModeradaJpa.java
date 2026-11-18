package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;
import java.util.UUID;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;
import jakarta.persistence.*;
/** Keeps original attachment URLs protected even after an author edits a moderated post. */
@Entity @Table(name="moderacao_midias",uniqueConstraints=@UniqueConstraint(name="uk_moderacao_midia_alvo_url",columnNames={"tipo","conteudo_id","url"}),indexes=@Index(name="ix_moderacao_midia_url",columnList="url"))
public class MidiaModeradaJpa {
 @Id public UUID id;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) public TipoConteudo tipo;
 @Column(name="conteudo_id",nullable=false) public UUID conteudoId;
 @Column(nullable=false,length=1000) public String url;
 protected MidiaModeradaJpa() {}
 public MidiaModeradaJpa(TipoConteudo tipo,UUID conteudoId,String url) { this.id=UUID.randomUUID();this.tipo=tipo;this.conteudoId=conteudoId;this.url=url; }
}
