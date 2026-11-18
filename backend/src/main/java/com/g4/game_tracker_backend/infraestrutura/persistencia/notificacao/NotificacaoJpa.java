package com.g4.game_tracker_backend.infraestrutura.persistencia.notificacao;
import java.time.Instant;
import java.util.UUID;
import com.g4.game_tracker_backend.dominio.notificacao.*;
import jakarta.persistence.*;
@Entity
@Table(name="notificacoes",uniqueConstraints=@UniqueConstraint(name="uk_notificacoes_origem",columnNames="contexto_origem"))
class NotificacaoJpa {
    @Id UUID id;
    @Column(name="destinatario_id",nullable=false) UUID destinatarioId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) TipoNotificacao tipo;
    @Column(nullable=false,length=160) String titulo;
    @Column(nullable=false,length=1000) String mensagem;
    @Column(name="contexto_origem",nullable=false,length=160) String contextoOrigem;
    @Column(name="gerada_em",nullable=false) Instant geradaEm;
    @Column(name="lida_em") Instant lidaEm;
    protected NotificacaoJpa() {}
    NotificacaoJpa(Notificacao n) {
        id=n.id(); destinatarioId=n.destinatarioId(); tipo=n.tipo(); titulo=n.titulo();
        mensagem=n.mensagem(); contextoOrigem=n.contextoOrigem(); geradaEm=n.geradaEm(); lidaEm=n.lidaEm();
    }
    Notificacao dominio() { return new Notificacao(id,destinatarioId,tipo,titulo,mensagem,contextoOrigem,geradaEm,lidaEm); }
}
