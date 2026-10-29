package com.g4.game_tracker_backend.infraestrutura.seguranca;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.aplicacao.identidade.AutenticadorDeCredenciais;
import com.g4.game_tracker_backend.dominio.compartilhado.CredenciaisInvalidasException;

@Component
public class AutenticadorSpringSecurity implements AutenticadorDeCredenciais {

    private final AuthenticationManager authenticationManager;

    public AutenticadorSpringSecurity(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void autenticar(String email, String senha) {
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, senha));
        } catch (AuthenticationException e) {
            throw new CredenciaisInvalidasException();
        }
    }
}
