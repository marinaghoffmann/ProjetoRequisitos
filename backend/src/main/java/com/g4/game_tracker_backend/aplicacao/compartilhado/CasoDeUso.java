package com.g4.game_tracker_backend.aplicacao.compartilhado;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca uma classe da camada de aplicação (caso de uso, consulta ou observador) que a infraestrutura deve
 * instanciar e injetar. É Java puro: quem lê a marca é o {@code CasosDeUsoConfig}, na infraestrutura, e a
 * aplicação não depende do Spring.
 *
 * <p>Classes que precisam de montagem especial (por exemplo, dependências opcionais) não levam a marca e
 * são criadas por um {@code XxxConfig}, como as da negociação e dos pedidos.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CasoDeUso {
}
