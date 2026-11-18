package com.g4.game_tracker_backend.dominio.avaliacao.reviews;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * RN 1: o usuário já avaliou o jogo. Nenhuma review nova é criada; quem chamou recebe o id da review
 * existente para redirecionar o usuário à edição.
 */
public class ReviewJaPublicadaException extends RegraDeNegocioException {

    private final UUID reviewExistenteId;

    public ReviewJaPublicadaException(UUID reviewExistenteId) {
        super("Você já publicou uma review deste jogo");
        this.reviewExistenteId = reviewExistenteId;
    }

    public UUID reviewExistenteId() {
        return reviewExistenteId;
    }
}
