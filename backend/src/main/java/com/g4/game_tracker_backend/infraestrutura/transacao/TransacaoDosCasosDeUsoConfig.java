package com.g4.game_tracker_backend.infraestrutura.transacao;

import java.lang.reflect.AnnotatedElement;

import org.springframework.aop.Advisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

/**
 * Liga a anotação {@link Transacional} da camada de aplicação ao gerenciador de transações do JPA. É o mesmo
 * mecanismo (proxy + interceptador) que o Spring usa para o {@code @Transactional}, só que lendo a anotação
 * do projeto. Assim os casos de uso não importam nada do Spring.
 */
@Configuration(proxyBeanMethods = false)
public class TransacaoDosCasosDeUsoConfig {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static Advisor transacaoDosCasosDeUso(BeanFactory beanFactory) {
        AnnotationTransactionAttributeSource fonte = new AnnotationTransactionAttributeSource(new LeitorDeTransacional());
        TransactionInterceptor interceptador = new TransactionInterceptor();
        interceptador.setTransactionAttributeSource(fonte);
        interceptador.setBeanFactory(beanFactory);
        BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setTransactionAttributeSource(fonte);
        advisor.setAdvice(interceptador);
        return advisor;
    }

    /** Traduz {@link Transacional} para os atributos de transação do Spring. */
    static final class LeitorDeTransacional implements TransactionAnnotationParser {

        @Override
        public boolean isCandidateClass(Class<?> classe) {
            return AnnotationUtils.isCandidateClass(classe, Transacional.class);
        }

        @Override
        public TransactionAttribute parseTransactionAnnotation(AnnotatedElement elemento) {
            Transacional anotacao = AnnotatedElementUtils.findMergedAnnotation(elemento, Transacional.class);
            if (anotacao == null) {
                return null;
            }
            // Sem regras extras: rollback em RuntimeException e Error, como no @Transactional.
            RuleBasedTransactionAttribute atributo = new RuleBasedTransactionAttribute();
            atributo.setReadOnly(anotacao.somenteLeitura());
            atributo.setPropagationBehavior(switch (anotacao.propagacao()) {
                case PARTICIPAR -> TransactionDefinition.PROPAGATION_REQUIRED;
                case NOVA -> TransactionDefinition.PROPAGATION_REQUIRES_NEW;
                case EXIGIR_EXISTENTE -> TransactionDefinition.PROPAGATION_MANDATORY;
            });
            return atributo;
        }
    }
}
