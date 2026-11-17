package com.g4.game_tracker_backend.dominio.biblioteca.conclusoes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

public record RegistroDeConclusao(UUID id, UUID jogadorId, UUID jogoId, LocalDate dataConclusao,
        String plataforma, BigDecimal horasJogadas, boolean concluido, boolean platina, Instant registradoEm) {

    public record Dados(LocalDate dataConclusao, String plataforma, BigDecimal horasJogadas,
            boolean concluido, boolean platina) {
    }

    public static RegistroDeConclusao registrar(UUID jogadorId, UUID jogoId, Dados dados, LocalDate hoje,
            Instant agora) {
        return validar(UUID.randomUUID(), jogadorId, jogoId, dados, hoje, agora);
    }

    public RegistroDeConclusao editar(UUID novoJogoId, Dados dados, LocalDate hoje) {
        return validar(id, jogadorId, novoJogoId, dados, hoje, registradoEm);
    }

    private static RegistroDeConclusao validar(UUID id, UUID jogadorId, UUID jogoId, Dados dados,
            LocalDate hoje, Instant registradoEm) {
        if (dados.dataConclusao() == null) {
            throw new RegraDeNegocioException("A data de conclusão é obrigatória");
        }
        if (dados.dataConclusao().isAfter(hoje)) {
            throw new RegraDeNegocioException("A data de conclusão não pode ser futura");
        }

        BigDecimal horas = dados.horasJogadas();
        if (horas != null) {
            if (horas.signum() <= 0) {
                throw new RegraDeNegocioException("As horas jogadas devem ser maiores que zero");
            }
            horas = horas.stripTrailingZeros();
            if (horas.scale() > 6 || horas.compareTo(new BigDecimal("10000000000000")) >= 0) {
                throw new RegraDeNegocioException("Informe as horas com até 13 dígitos inteiros e 6 decimais");
            }
        }
        if (dados.platina() && !dados.concluido()) {
            throw new RegraDeNegocioException("A platina exige que o jogo esteja concluído");
        }

        String plataforma = dados.plataforma() == null ? null : dados.plataforma().strip();
        if (plataforma != null && plataforma.length() > 100) {
            throw new RegraDeNegocioException("A plataforma deve ter até 100 caracteres");
        }
        if ("".equals(plataforma)) {
            plataforma = null;
        }

        return new RegistroDeConclusao(id, jogadorId, jogoId, dados.dataConclusao(), plataforma, horas,
                dados.concluido(), dados.platina(), registradoEm);
    }
}
