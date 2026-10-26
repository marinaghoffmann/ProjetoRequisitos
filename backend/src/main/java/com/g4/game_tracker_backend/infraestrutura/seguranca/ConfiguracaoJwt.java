package com.g4.game_tracker_backend.infraestrutura.seguranca;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class ConfiguracaoJwt {

    @Bean
    SecretKey chaveJwt(@Value("${app.security.jwt.secret-base64}") String chaveBase64) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(chaveBase64);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("APP_SECURITY_JWT_SECRET deve ser uma chave Base64 válida", e);
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("A chave JWT deve conter pelo menos 32 bytes aleatórios");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey chaveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chaveJwt));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey chaveJwt) {
        return org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withSecretKey(chaveJwt)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
