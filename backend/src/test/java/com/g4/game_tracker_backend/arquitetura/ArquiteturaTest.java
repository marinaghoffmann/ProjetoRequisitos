package com.g4.game_tracker_backend.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * Regras da arquitetura limpa verificadas a cada build. Se um destes testes falhar,
 * alguma classe está importando de uma camada que não deveria.
 */
class ArquiteturaTest {

    private static final String BASE = "com.g4.game_tracker_backend";

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE);

    @Test
    void dominioNaoDependeDeOutrasCamadas() {
        noClasses().that().resideInAPackage("..dominio..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..aplicacao..", "..infraestrutura..", "..apresentacao..")
                .check(CLASSES);
    }

    @Test
    void dominioNaoDependeDeFrameworks() {
        noClasses().that().resideInAPackage("..dominio..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "org.hibernate..")
                .because("o domínio é Java puro; as entidades JPA ficam em infraestrutura.persistencia")
                .check(CLASSES);
    }

    @Test
    void aplicacaoNaoDependeDeInfraestruturaNemApresentacao() {
        noClasses().that().resideInAPackage("..aplicacao..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infraestrutura..", "..apresentacao..", "jakarta.persistence..")
                .check(CLASSES);
    }

    /**
     * Meta da arquitetura limpa: casos de uso em Java puro, com transação e eventos atrás de portas
     * ({@code ExecutorDeTransacao}, {@code PublicadorDeEventos}) e wiring em {@code infraestrutura.configuracao}.
     * Os contextos abaixo já foram migrados. Ao migrar outro contexto, inclua o pacote dele na lista; quando
     * todos estiverem aqui, troque a lista por "..aplicacao..".
     */
    @Test
    void aplicacaoMigradaNaoDependeDoSpring() {
        noClasses().that().resideInAnyPackage(
                        "..aplicacao.compartilhado..",
                        "..aplicacao.marketplace.negociacao..",
                        "..aplicacao.marketplace.pedidos..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..")
                .because("os casos de uso não dependem de framework; o Spring entra só pela infraestrutura")
                .check(CLASSES);
    }

    @Test
    void apresentacaoNaoAcessaInfraestrutura() {
        noClasses().that().resideInAPackage("..apresentacao..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infraestrutura..")
                .because("controllers chamam casos de uso da aplicação, nunca repositórios")
                .check(CLASSES);
    }
}
