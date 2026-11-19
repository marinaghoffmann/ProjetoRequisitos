package com.g4.game_tracker_backend.infraestrutura.configuracao;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.g4.game_tracker_backend.aplicacao.catalogo.BuscadorDeCapturas;
import com.g4.game_tracker_backend.aplicacao.catalogo.CapturasEmCache;
import com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo.SteamCapturas;

/** F-01: o ConsultarCapturasJogo recebe as capturas da Steam por trás do proxy de cache. */
@Configuration(proxyBeanMethods = false)
public class CatalogoConfig {

    @Bean
    BuscadorDeCapturas buscadorDeCapturas(
            @Value("${app.catalogo.steam-url:https://store.steampowered.com}") String urlDaSteam,
            @Value("${app.catalogo.capturas-cache-validade:PT24H}") Duration validade) {
        return new CapturasEmCache(new SteamCapturas(urlDaSteam), Clock.systemUTC(), validade);
    }
}
