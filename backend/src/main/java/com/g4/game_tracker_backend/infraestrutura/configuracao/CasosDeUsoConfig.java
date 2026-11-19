package com.g4.game_tracker_backend.infraestrutura.configuracao;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;

/**
 * Registra como beans as classes da aplicação marcadas com {@link CasoDeUso}. A dependência vai da
 * infraestrutura para a aplicação, nunca o contrário. As que precisam de montagem especial ficam nos
 * outros {@code XxxConfig} deste pacote.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "com.g4.game_tracker_backend.aplicacao", useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = CasoDeUso.class))
public class CasosDeUsoConfig {
}
