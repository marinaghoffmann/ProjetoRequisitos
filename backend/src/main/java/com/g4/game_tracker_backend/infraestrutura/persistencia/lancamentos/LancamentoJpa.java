package com.g4.game_tracker_backend.infraestrutura.persistencia.lancamentos;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.g4.game_tracker_backend.dominio.lancamentos.*;
import jakarta.persistence.*;

@Entity
@Table(name="lancamentos_futuros", uniqueConstraints=@UniqueConstraint(name="uk_lancamento_jogo",columnNames="jogo_id"))
class LancamentoJpa {
    @Id UUID id;
    @Column(name="jogo_id",nullable=false) UUID jogoId;
    @Column(name="data_prevista",nullable=false) LocalDate dataPrevista;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) StatusLancamento status;
    @Column(name="cadastrado_em",nullable=false) Instant cadastradoEm;
    @Enumerated(EnumType.STRING) @Column(name="tipo_data",length=16) TipoDataLancamento tipoData;
    @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
    @JoinColumn(name="lancamento_id")
    List<AlteracaoCronogramaJpa> historico = new ArrayList<>();
    protected LancamentoJpa() {}
    LancamentoJpa(LancamentoFuturo l) {
        id=l.id(); jogoId=l.jogoId(); dataPrevista=l.dataPrevista(); status=l.status();
        cadastradoEm=l.cadastradoEm(); tipoData=l.tipoData();
        historico=new ArrayList<>(l.historico().stream().map(AlteracaoCronogramaJpa::new).toList());
    }
    LancamentoFuturo dominio() {
        return new LancamentoFuturo(id,jogoId,dataPrevista,status,cadastradoEm,
                historico.stream().map(AlteracaoCronogramaJpa::dominio).toList(),
                tipoData == null ? TipoDataLancamento.CONFIRMADA : tipoData);
    }
}
@Entity
@Table(name="lancamentos_alteracoes")
class AlteracaoCronogramaJpa {
    @Id UUID id;
    @Column(name="data_anterior",nullable=false) LocalDate dataAnterior;
    @Column(name="nova_data",nullable=false) LocalDate novaData;
    @Column(name="alterado_por_id") UUID alteradoPorId;
    @Column(name="alterado_em",nullable=false) Instant alteradoEm;
    protected AlteracaoCronogramaJpa() {}
    AlteracaoCronogramaJpa(AlteracaoCronograma a) {
        id=a.id(); dataAnterior=a.dataAnterior(); novaData=a.novaData();
        alteradoPorId=a.alteradoPorId(); alteradoEm=a.alteradoEm();
    }
    AlteracaoCronograma dominio() { return new AlteracaoCronograma(id,dataAnterior,novaData,alteradoPorId,alteradoEm); }
}
