package com.g4.game_tracker_backend.dominio.compartilhado;

/**
 * Contrato do Decorator de exibição: o texto que o leitor vê. Usado pela máscara de spoiler (F-15)
 * e pela mensagem de conteúdo removido pela moderação (F-14).
 */
public interface IConteudoExibivel {
    String exibir();
}
