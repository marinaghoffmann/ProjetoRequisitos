package com.g4.game_tracker_backend.aplicacao.compartilhado;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Forma declarativa da porta {@link ExecutorDeTransacao}: o método (ou todos os métodos públicos da classe)
 * roda numa transação. É Java puro; a infraestrutura ({@code TransacaoDosCasosDeUsoConfig}) aplica a
 * transação com a mesma semântica do {@code @Transactional} do Spring: rollback em qualquer
 * {@link RuntimeException} e participação na transação já aberta.
 *
 * <p>Use a porta quando o caso de uso precisar de várias transações num mesmo método (por exemplo, uma por
 * item de um lote) e a anotação quando o método inteiro for uma transação.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Transacional {

    /** Transação só de leitura (consultas). */
    boolean somenteLeitura() default false;

    Propagacao propagacao() default Propagacao.PARTICIPAR;

    enum Propagacao {
        /** Participa da transação aberta ou abre uma nova (REQUIRED). */
        PARTICIPAR,
        /** Sempre abre uma transação nova, suspendendo a atual (REQUIRES_NEW). */
        NOVA,
        /** Falha se não houver transação aberta (MANDATORY): só roda dentro da ação de outro caso de uso. */
        EXIGIR_EXISTENTE
    }
}
