package com.g4.game_tracker_backend.dominio.compartilhado;

/** Indica que o email ou a senha informados no login não conferem. */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("Email ou senha inválidos");
    }
}
