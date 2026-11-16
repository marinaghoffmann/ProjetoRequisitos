package com.g4.game_tracker_backend.infraestrutura.persistencia.lancamentos;
import java.time.Instant;
import java.util.UUID;
import com.g4.game_tracker_backend.dominio.lancamentos.NotificacaoLancamento;
import jakarta.persistence.*;
@Entity
@Table(name="lancamentos_alertas",indexes=@Index(name="ix_lancamentos_alertas_disparo",columnList="disparar_em"))
class AlertaJpa {
    @Id UUID id;
    @Column(name="interesse_id",nullable=false) UUID interesseId;
    @Column(name="disparar_em",nullable=false) Instant dispararEm;
    @Column(nullable=false) boolean enviada;
    protected AlertaJpa() {}
    AlertaJpa(NotificacaoLancamento a) {
        id=a.id(); interesseId=a.interesseId(); dispararEm=a.dispararEm(); enviada=a.enviada();
    }
    NotificacaoLancamento dominio() { return new NotificacaoLancamento(id,interesseId,dispararEm,enviada); }
}
