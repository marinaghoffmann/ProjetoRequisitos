package com.g4.game_tracker_backend.dominio.lancamentos;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

public record LancamentoFuturo(UUID id, UUID jogoId, LocalDate dataPrevista, StatusLancamento status,
        Instant cadastradoEm, List<AlteracaoCronograma> historico, TipoDataLancamento tipoData) {
    public LancamentoFuturo {
        Objects.requireNonNull(id);
        Objects.requireNonNull(jogoId);
        Objects.requireNonNull(dataPrevista);
        Objects.requireNonNull(status);
        Objects.requireNonNull(cadastradoEm);
        Objects.requireNonNull(tipoData);
        historico = List.copyOf(historico);
    }
    public LancamentoFuturo(UUID id, UUID jogoId, LocalDate dataPrevista, StatusLancamento status,
            Instant cadastradoEm, List<AlteracaoCronograma> historico) {
        this(id, jogoId, dataPrevista, status, cadastradoEm, historico, TipoDataLancamento.CONFIRMADA);
    }
    public static LancamentoFuturo criar(UUID jogoId, LocalDate data, LocalDate hoje, Instant agora) {
        return criar(jogoId, data, hoje, agora, TipoDataLancamento.CONFIRMADA);
    }
    public static LancamentoFuturo criar(UUID jogoId, LocalDate data, LocalDate hoje, Instant agora,
            TipoDataLancamento tipoData) {
        exigirFutura(data, hoje);
        return new LancamentoFuturo(UUID.randomUUID(), jogoId, data,
                StatusLancamento.FUTURO_LANCAMENTO, agora, List.of(), Objects.requireNonNull(tipoData));
    }
    public LancamentoFuturo alterarData(LocalDate nova, UUID autor, LocalDate hoje, Instant agora) {
        if (nova == null) throw new RegraDeNegocioException("A data de lançamento é obrigatória");
        if (nova.equals(dataPrevista)) return this;
        List<AlteracaoCronograma> alteracoes = new ArrayList<>(historico);
        alteracoes.add(new AlteracaoCronograma(UUID.randomUUID(), dataPrevista, nova, autor, agora));
        return new LancamentoFuturo(id, jogoId, nova,
                tipoData == TipoDataLancamento.ESTIMADA || nova.isAfter(hoje)
                        ? StatusLancamento.FUTURO_LANCAMENTO : StatusLancamento.LANCADO,
                cadastradoEm, alteracoes, tipoData);
    }
    public LancamentoFuturo comTipoData(TipoDataLancamento novoTipo, LocalDate hoje) {
        Objects.requireNonNull(novoTipo);
        if (novoTipo == tipoData) return this;
        StatusLancamento novoStatus = novoTipo == TipoDataLancamento.ESTIMADA || dataPrevista.isAfter(hoje)
                ? StatusLancamento.FUTURO_LANCAMENTO : StatusLancamento.LANCADO;
        return new LancamentoFuturo(id, jogoId, dataPrevista, novoStatus, cadastradoEm, historico, novoTipo);
    }
    public LancamentoFuturo lancar() {
        if (status == StatusLancamento.LANCADO || tipoData == TipoDataLancamento.ESTIMADA) return this;
        return new LancamentoFuturo(id, jogoId, dataPrevista, StatusLancamento.LANCADO,
                cadastradoEm, historico, tipoData);
    }
    public static void exigirFutura(LocalDate data, LocalDate hoje) {
        if (data == null || !data.isAfter(hoje))
            throw new RegraDeNegocioException("A data de lançamento deve ser futura");
    }
}
