package com.g4.game_tracker_backend.dominio.avaliacao.estatisticas;

/** Notas das reviews ativas de um jogo, como a F-02 deve entregá-las à engine de estatísticas. */
public record NotasDoJogo(
        NotasAgregadas geral,
        NotasAgregadas graficos,
        NotasAgregadas som,
        NotasAgregadas jogabilidade,
        NotasAgregadas diversao,
        NotasAgregadas inovacao) {

    public static NotasDoJogo vazias() {
        NotasAgregadas vazias = NotasAgregadas.vazias();
        return new NotasDoJogo(vazias, vazias, vazias, vazias, vazias, vazias);
    }

    /** Cada review ativa tem exatamente uma nota geral. */
    public int totalReviewsAtivas() {
        return geral.quantidade();
    }
}
