package com.g4.game_tracker_backend.aplicacao.identidade;

/** Porta para armazenar e verificar senhas sem acoplar a aplicação a um algoritmo. */
public interface CodificadorDeSenha {

    String codificar(String senhaEmTextoClaro);
}
