package com.g4.game_tracker_backend.dominio.avaliacao.reviews;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/** Nota de 1 a 5, usada tanto na nota geral quanto nas notas por categoria. */
public record NotaAvaliacao(int valor) {

    public static final int MINIMA = 1;
    public static final int MAXIMA = 5;
    static final String FORA_DO_INTERVALO = "A nota deve estar entre 1 e 5";

    public NotaAvaliacao {
        if (valor < MINIMA || valor > MAXIMA) {
            throw new RegraDeNegocioException(FORA_DO_INTERVALO);
        }
    }

    /** Converte o valor informado pelo usuário; nota ausente também é recusada. */
    public static NotaAvaliacao de(Integer valor) {
        if (valor == null) {
            throw new RegraDeNegocioException(FORA_DO_INTERVALO);
        }
        return new NotaAvaliacao(valor);
    }
}
