package com.g4.game_tracker_backend.infraestrutura.seguranca;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.aplicacao.identidade.CodificadorDeSenha;

@Component
public class CodificadorDeSenhaBCrypt implements CodificadorDeSenha {

    private final PasswordEncoder passwordEncoder;

    public CodificadorDeSenhaBCrypt(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String codificar(String senhaEmTextoClaro) {
        return passwordEncoder.encode(senhaEmTextoClaro);
    }

}
