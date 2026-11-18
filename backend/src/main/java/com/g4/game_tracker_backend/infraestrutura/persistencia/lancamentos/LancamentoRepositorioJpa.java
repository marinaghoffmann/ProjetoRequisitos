package com.g4.game_tracker_backend.infraestrutura.persistencia.lancamentos;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.g4.game_tracker_backend.dominio.lancamentos.*;
interface LancamentoSpringData extends JpaRepository<LancamentoJpa,UUID> {
    Optional<LancamentoJpa> findByJogoId(UUID jogoId);
    List<LancamentoJpa> findByStatusOrderByDataPrevistaAscIdAsc(StatusLancamento status);
    List<LancamentoJpa> findByStatusOrderByDataPrevistaDescIdAsc(StatusLancamento status);
    List<LancamentoJpa> findByStatusAndDataPrevistaLessThanEqual(StatusLancamento status,LocalDate hoje);
}
@Repository
public class LancamentoRepositorioJpa implements LancamentoRepositorio {
    private final LancamentoSpringData dados;
    public LancamentoRepositorioJpa(LancamentoSpringData dados) { this.dados=dados; }
    public LancamentoFuturo salvar(LancamentoFuturo l) { dados.save(new LancamentoJpa(l)); return l; }
    public Optional<LancamentoFuturo> buscar(UUID id) { return dados.findById(id).map(LancamentoJpa::dominio); }
    public Optional<LancamentoFuturo> buscarPorJogo(UUID id) { return dados.findByJogoId(id).map(LancamentoJpa::dominio); }
    public List<LancamentoFuturo> listarFuturos() {
        return dados.findByStatusOrderByDataPrevistaAscIdAsc(StatusLancamento.FUTURO_LANCAMENTO)
                .stream().map(LancamentoJpa::dominio).toList();
    }
    public List<LancamentoFuturo> listarLancados() {
        return dados.findByStatusOrderByDataPrevistaDescIdAsc(StatusLancamento.LANCADO)
                .stream().map(LancamentoJpa::dominio).toList();
    }
    public List<LancamentoFuturo> listarVencidos(LocalDate hoje) {
        return dados.findByStatusAndDataPrevistaLessThanEqual(StatusLancamento.FUTURO_LANCAMENTO,hoje)
                .stream().map(LancamentoJpa::dominio).toList();
    }
    public void excluir(UUID id) { dados.deleteById(id); }
}
