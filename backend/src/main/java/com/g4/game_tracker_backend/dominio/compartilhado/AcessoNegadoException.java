package com.g4.game_tracker_backend.dominio.compartilhado;

/**
 * Lançada quando o usuário não tem papel ou propriedade para executar a ação
 * (por exemplo, editar a review de outra pessoa ou acessar a central de moderação).
 */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
