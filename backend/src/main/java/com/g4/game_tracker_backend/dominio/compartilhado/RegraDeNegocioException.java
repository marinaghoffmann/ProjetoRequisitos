package com.g4.game_tracker_backend.dominio.compartilhado;

/**
 * Lançada quando uma regra de negócio (RN) é violada. A mensagem é exibida ao usuário
 * e conferida nos cenários BDD ("deve ser recusado com a mensagem ..."), por isso deve
 * ser escrita em português e de forma estável.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
