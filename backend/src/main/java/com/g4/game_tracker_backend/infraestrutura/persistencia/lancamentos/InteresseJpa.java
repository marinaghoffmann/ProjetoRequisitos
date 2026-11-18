package com.g4.game_tracker_backend.infraestrutura.persistencia.lancamentos;
import java.time.Instant;
import java.util.UUID;
import com.g4.game_tracker_backend.dominio.lancamentos.InteresseEmLancamento;
import jakarta.persistence.*;
@Entity
@Table(name="lancamentos_interesses",uniqueConstraints=@UniqueConstraint(name="uk_interesse_usuario_jogo",columnNames={"usuario_id","jogo_id"}))
class InteresseJpa {
    @Id UUID id;
    @Column(name="usuario_id",nullable=false) UUID usuarioId;
    @Column(name="jogo_id",nullable=false) UUID jogoId;
    @Column(nullable=false) boolean ativo;
    @Column(name="registrado_em",nullable=false) Instant registradoEm;
    protected InteresseJpa() {}
    InteresseJpa(InteresseEmLancamento i) {
        id=i.id(); usuarioId=i.usuarioId(); jogoId=i.jogoId(); ativo=i.ativo(); registradoEm=i.registradoEm();
    }
    InteresseEmLancamento dominio() { return new InteresseEmLancamento(id,usuarioId,jogoId,ativo,registradoEm); }
}
