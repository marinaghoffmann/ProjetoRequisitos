package com.g4.game_tracker_backend.infraestrutura.seguranca;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.aplicacao.identidade.EmissorDeToken;
import com.g4.game_tracker_backend.aplicacao.identidade.TokenDeAcesso;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;

@Component
public class EmissorJwt implements EmissorDeToken {

    private final JwtEncoder jwtEncoder;
    private final long duracaoMinutos;

    public EmissorJwt(JwtEncoder jwtEncoder,
            @Value("${app.security.jwt.expiration-minutes:60}") long duracaoMinutos) {
        this.jwtEncoder = jwtEncoder;
        this.duracaoMinutos = duracaoMinutos;
    }

    @Override
    public TokenDeAcesso emitir(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(duracaoMinutos, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("game-tracker")
                .issuedAt(agora)
                .expiresAt(expiracao)
                .subject(usuario.id().toString())
                .claim("email", usuario.email())
                .claim("papel", usuario.papel().name())
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenDeAcesso(token, expiracao);
    }
}
