package com.g4.game_tracker_backend.aplicacao.identidade;

/** Porta para autenticar as credenciais submetidas no login. */
public interface AutenticadorDeCredenciais {

    void autenticar(String email, String senha);
}
